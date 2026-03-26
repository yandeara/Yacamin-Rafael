package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.application.service.algoritms.simulation.SimulationMarketMemoryService;
import br.com.yacamin.rafael.domain.BlockDuration;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.InferencePrediction;
import br.com.yacamin.rafael.domain.Market;
import br.com.yacamin.rafael.domain.ModelDescriptor;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Lógica MINUTE-A-MINUTE: roda inferência XGBoost cada vez que um candle de 1m fecha.
 *
 * Fluxo por candle:
 * 1. Resolve a previsão anterior (preenche closeMid + hit)
 * 2. Determina qual bloco de 5m e qual minuto dentro do bloco
 * 3. Extrai features → roda XGBoost → cria nova previsão com openMid
 *
 * Alinhamento temporal (bloco 10:00-10:05):
 * - Candle 10:00 fecha → resolve nada (primeira). Cria pred 1 (openMid=close_10:00, prevê 10:00→10:01)
 * - Candle 10:01 fecha → resolve pred 1 (closeMid=close_10:01). Cria pred 2 (prevê 10:01→10:02)
 * - Candle 10:02 fecha → resolve pred 2. Cria pred 3.
 * - Candle 10:03 fecha → resolve pred 3. Cria pred 4.
 * - Candle 10:04 fecha → resolve pred 4. Cria pred 5 (prevê 10:04→10:05)
 * - Candle 10:05 fecha → resolve pred 5 do bloco anterior. Cria pred 1 do NOVO bloco.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinuteByMinuteInferenceService {

    private static final String DEFAULT_MODEL_KEY = "xgb_BTCUSDT_h1_1m";
    private static final BlockDuration BLOCK_DURATION = BlockDuration.FIVE_MIN;
    private static final String ALGO = "RAFAEL";

    private final ModelRegistryService modelRegistryService;
    private final FeatureExtractorService featureExtractorService;
    private final BlockInferenceMemoryService blockInferenceMemory;
    private final PredictionEventService predictionEventService;
    private final SimulationMarketMemoryService simulationMarketMemoryService;

    @Getter
    private volatile boolean live = false;

    public void setLive(boolean live) {
        this.live = live;
        log.info("[M2M] Live mode: {}", live);
    }

    public void onCandleClosed(SymbolCandle candle) {
        if (!live) return;
        if (candle.getInterval() != CandleIntervals.I1_MN) return;

        var descriptorOpt = modelRegistryService.getByKey(DEFAULT_MODEL_KEY);
        if (descriptorOpt.isEmpty()) return;

        ModelDescriptor descriptor = descriptorOpt.get();
        if (!candle.getSymbol().equalsIgnoreCase(descriptor.symbol())) return;

        try {
            double candleClose = candle.getClose();
            double candleOpen = candle.getOpen();
            Instant openTime = candle.getOpenTime();
            Instant closeTime = openTime.plusSeconds(60);

            log.debug("[M2M] Candle received: openTime={} closeTime={} open={} close={}",
                    openTime, closeTime, candleOpen, candleClose);

            // 1. Resolver a previsão anterior
            resolvePreviousPrediction(closeTime, candleClose);

            // 2. Determinar bloco e minuto usando closeTime
            //    Candle openTime=12:44 closeTime=12:45 → bloco 12:45, minuto 1
            //    Candle openTime=12:45 closeTime=12:46 → bloco 12:45, minuto 2
            //    Candle openTime=12:49 closeTime=12:50 → bloco 12:50, minuto 1 (novo bloco)
            long blockUnix = BLOCK_DURATION.currentBlockUnix(closeTime);
            long secondsInBlock = closeTime.getEpochSecond() - blockUnix;
            int minuteInBlock = (int) (secondsInBlock / 60) + 1;

            // No closeTime exato de boundary (12:45:00), secondsInBlock=0, minuteInBlock=1
            if (secondsInBlock == 0) minuteInBlock = 1;

            if (minuteInBlock < 1 || minuteInBlock > 5) {
                log.warn("[M2M] minuteInBlock={} out of range for closeTime={}", minuteInBlock, closeTime);
                return;
            }

            // 3. Extrair features (usa openTime pois é assim que estão indexadas no Scylla)
            long t0 = System.currentTimeMillis();

            float[] features = featureExtractorService.extractFeatures(
                    candle.getSymbol(), openTime, CandleIntervals.I1_MN);

            if (features == null || features.length == 0) {
                log.warn("[M2M] No features for {} @ {}", candle.getSymbol(), openTime);
                return;
            }

            Booster booster = modelRegistryService.getBooster(descriptor.key());
            float[] probs = predict(booster, features);

            if (probs == null || probs.length < 2) {
                log.warn("[M2M] Invalid probs for {} @ {}", candle.getSymbol(), openTime);
                return;
            }

            // 4. Encontrar argmax
            int argmax = 0;
            for (int i = 1; i < probs.length; i++) {
                if (probs[i] > probs[argmax]) argmax = i;
            }

            // 5. Interpretar classe seguindo convenção Mikhael:
            //    Modelo binário (2 classes):  argmax 0 = DOWN, argmax 1 = UP
            //    Modelo multiclass (3+ classes): argmax 1 = DOWN, argmax 2 = UP
            String direction;
            if (probs.length == 2) {
                direction = argmax == 0 ? "DOWN" : "UP";
            } else {
                // Convenção Mikhael: classe 1 = DOWN, classe 2 = UP
                direction = argmax == 2 ? "UP" : "DOWN";
            }
            double confidence = probs[argmax];

            // Log completo das probabilidades para debug
            StringBuilder probsStr = new StringBuilder("[");
            for (int i = 0; i < probs.length; i++) {
                if (i > 0) probsStr.append(", ");
                probsStr.append(String.format("c%d=%.4f", i, probs[i]));
            }
            probsStr.append("]");

            // 6. Criar previsão
            //    openMid = close do candle (preço no momento da previsão, pra visualização)
            //    modelThreshold = open do candle (o que o modelo compara: close_proximo vs open_atual)
            double modelThreshold = candle.getOpen();

            InferencePrediction prediction = new InferencePrediction(
                    direction, confidence, minuteInBlock, openTime, descriptor.key(), candleClose);
            prediction.setModelThreshold(modelThreshold);

            blockInferenceMemory.addPrediction(blockUnix, prediction);

            // Gravar evento PREDICTION_M2M
            Market market = simulationMarketMemoryService.findMarketByUnixTime(ALGO, blockUnix);
            String slug = market != null ? market.getSlug() : "unknown-" + blockUnix;
            String mktGroup = market != null ? market.getMarketGroup() : "unknown";
            predictionEventService.recordPrediction(slug, mktGroup, "PREDICTION_M2M", prediction);

            long elapsed = System.currentTimeMillis() - t0;
            log.info("[M2M] {} block={} min={} -> {} ({}) threshold={} argmax={} probs={} [{}ms]",
                    candle.getSymbol(), blockUnix, minuteInBlock, direction, confidence,
                    modelThreshold, argmax, probsStr, elapsed);

        } catch (Exception e) {
            log.error("[M2M] Inference failed for {} @ {}: {}",
                    candle.getSymbol(), candle.getOpenTime(), e.getMessage(), e);
        }
    }

    /**
     * Resolve a última previsão pendente.
     * O candle que acabou de fechar nos dá o closeMid da previsão anterior.
     *
     * A previsão anterior foi feita no closeTime anterior (= currentCloseTime - 1min).
     * Usamos esse closeTime anterior para achar o bloco correto.
     */
    private void resolvePreviousPrediction(Instant currentCloseTime, double currentCandleClose) {
        // O closeTime do candle anterior = currentCloseTime - 1min
        Instant prevCloseTime = currentCloseTime.minusSeconds(60);
        long prevBlockUnix = BLOCK_DURATION.currentBlockUnix(prevCloseTime);

        List<InferencePrediction> prevBlockPreds = blockInferenceMemory.getPredictions(prevBlockUnix);
        if (prevBlockPreds.isEmpty()) return;

        InferencePrediction lastPred = prevBlockPreds.getLast();
        if (lastPred.getCloseMid() != null) return;

        lastPred.resolve(currentCandleClose);

        log.info("[M2M] Resolved block={} min={}: {} openMid={} closeMid={} -> {}",
                prevBlockUnix, lastPred.getMinuteInBlock(), lastPred.getDirection(),
                lastPred.getOpenMid(), currentCandleClose, lastPred.getHit() ? "HIT" : "MISS");

        // Gravar evento PREDICTION_M2M_RESOLVED
        Market market = simulationMarketMemoryService.findMarketByUnixTime(ALGO, prevBlockUnix);
        String slug = market != null ? market.getSlug() : "unknown-" + prevBlockUnix;
        String mktGroup = market != null ? market.getMarketGroup() : "unknown";
        predictionEventService.recordM2mResolved(slug, mktGroup, lastPred);
    }

    private float[] predict(Booster booster, float[] features) {
        try {
            DMatrix mat = new DMatrix(features, 1, features.length, Float.NaN);
            float[][] prob2d = booster.predict(mat);
            if (prob2d == null || prob2d.length == 0) return null;
            return prob2d[0];
        } catch (XGBoostError e) {
            log.error("[M2M] XGBoost predict error: {}", e.getMessage(), e);
            return null;
        }
    }
}
