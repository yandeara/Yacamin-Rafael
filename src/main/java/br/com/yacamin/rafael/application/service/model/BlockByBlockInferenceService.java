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

/**
 * Lógica BLOCK-BY-BLOCK: roda inferência XGBoost cada vez que um candle de 5m fecha.
 *
 * Fluxo:
 * 1. Candle 5m fecha (ex: openTime=12:50, closeTime=12:55)
 * 2. Resolve a previsão do bloco anterior (se houver)
 * 3. Cria previsão para o PRÓXIMO bloco (12:55→13:00)
 *    - openMid = close do candle 5m atual (preço às 12:55)
 *    - closeMid = close do próximo candle 5m (preço às 13:00) — preenchido depois
 * 4. Armazena associada ao blockUnix do bloco previsto (12:55)
 *
 * Modelo default: xgb_BTCUSDT_h1_5m
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockByBlockInferenceService {

    private static final String DEFAULT_MODEL_KEY = "xgb_BTCUSDT_h1_5m";
    private static final BlockDuration BLOCK_DURATION = BlockDuration.FIVE_MIN;
    private static final String ALGO = "RAFAEL";

    private final ModelRegistryService modelRegistryService;
    private final FeatureExtractor5MnService featureExtractor5MnService;
    private final BlockInferenceMemoryService blockInferenceMemory;
    private final PredictionEventService predictionEventService;
    private final SimulationMarketMemoryService simulationMarketMemoryService;

    @Getter
    private volatile boolean live = false;

    public void setLive(boolean live) {
        this.live = live;
        log.info("[B2B] Live mode: {}", live);
    }

    public void onCandleClosed(SymbolCandle candle) {
        if (!live) return;
        if (candle.getInterval() != CandleIntervals.I5_MN) return;

        var descriptorOpt = modelRegistryService.getByKey(DEFAULT_MODEL_KEY);
        if (descriptorOpt.isEmpty()) return;

        ModelDescriptor descriptor = descriptorOpt.get();
        if (!candle.getSymbol().equalsIgnoreCase(descriptor.symbol())) return;

        try {
            double candleClose = candle.getClose();
            double candleOpen = candle.getOpen();
            Instant openTime = candle.getOpenTime();
            Instant closeTime = openTime.plusSeconds(300);

            log.debug("[B2B] Candle 5m received: openTime={} closeTime={} open={} close={}",
                    openTime, closeTime, candleOpen, candleClose);

            // 1. Resolver previsão anterior (hit candle)
            resolvePreviousPrediction(closeTime, candleClose);

            // 2. O bloco que estamos prevendo é o que COMEÇA no closeTime
            long predictedBlockUnix = BLOCK_DURATION.currentBlockUnix(closeTime);

            // 3. Extrair features (indexadas pelo openTime no Scylla)
            long t0 = System.currentTimeMillis();

            float[] features = featureExtractor5MnService.extractFeatures(
                    candle.getSymbol(), openTime, CandleIntervals.I5_MN);

            if (features == null || features.length == 0) {
                log.warn("[B2B] No features for {} @ {}", candle.getSymbol(), openTime);
                return;
            }

            Booster booster = modelRegistryService.getBooster(descriptor.key());
            float[] probs = predict(booster, features);

            if (probs == null || probs.length < 2) {
                log.warn("[B2B] Invalid probs for {} @ {}", candle.getSymbol(), openTime);
                return;
            }

            // 4. Interpretar (mesma convenção Mikhael)
            int argmax = 0;
            for (int i = 1; i < probs.length; i++) {
                if (probs[i] > probs[argmax]) argmax = i;
            }

            String direction;
            if (probs.length == 2) {
                direction = argmax == 0 ? "DOWN" : "UP";
            } else {
                direction = argmax == 2 ? "UP" : "DOWN";
            }
            double confidence = probs[argmax];

            // Log probs
            StringBuilder probsStr = new StringBuilder("[");
            for (int i = 0; i < probs.length; i++) {
                if (i > 0) probsStr.append(", ");
                probsStr.append(String.format("c%d=%.4f", i, probs[i]));
            }
            probsStr.append("]");

            // 5. Criar previsão e armazenar no bloco PREVISTO
            //    modelThreshold = open do candle analisado (o valor que o modelo compara)
            //    openMid = close do candle (preço no momento da previsão)
            double modelThreshold = candle.getOpen();

            InferencePrediction prediction = new InferencePrediction(
                    direction, confidence, 1, openTime, descriptor.key(), candleClose);
            prediction.setModelThreshold(modelThreshold);

            blockInferenceMemory.addBlockPrediction(predictedBlockUnix, prediction);

            // VALID/INVALID imediato: o openMid (close do candle 5m) É o preço
            // no exato segundo que o bloco começa. Não precisa esperar o Market.
            prediction.validateWithMarketOpen(candleClose);

            // Gravar evento PREDICTION_BLOCK
            Market market = simulationMarketMemoryService.findMarketByUnixTime(ALGO, predictedBlockUnix);
            String slug = market != null ? market.getSlug() : "unknown-" + predictedBlockUnix;
            String mktGroup = market != null ? market.getMarketGroup() : "unknown";
            predictionEventService.recordPrediction(slug, mktGroup, "PREDICTION_BLOCK", prediction);

            log.info("[B2B] {} predicts block={} -> {} ({}) threshold={} marketOpen(=openMid)={} valid={} argmax={} probs={} [{}ms]",
                    candle.getSymbol(), predictedBlockUnix, direction, confidence,
                    modelThreshold, candleClose, prediction.getValid(),
                    argmax, probsStr, System.currentTimeMillis() - t0);

        } catch (Exception e) {
            log.error("[B2B] Inference failed for {} @ {}: {}",
                    candle.getSymbol(), candle.getOpenTime(), e.getMessage(), e);
        }
    }

    /**
     * Quando o candle 5m atual fecha, resolve a previsão que foi feita 5 min atrás.
     *
     * O candle atual (openTime=12:05, closeTime=12:10) é o bloco que foi previsto.
     * - marketOpen = candle atual open (preço às 12:05, abertura do mercado)
     * - closeMid = candle atual close (preço às 12:10, fechamento do mercado)
     * - O bloco previsto começou em currentCloseTime - 5min
     */
    private void resolvePreviousPrediction(Instant currentCloseTime, double currentCandleClose) {
        long predictedBlockUnix = BLOCK_DURATION.currentBlockUnix(currentCloseTime.minusSeconds(300));

        InferencePrediction pred = blockInferenceMemory.getBlockPrediction(predictedBlockUnix);
        if (pred == null || pred.getCloseMid() != null) return;

        // Resolve candle: closeMid = close do candle 5m do bloco previsto
        // VALID/INVALID já foi preenchido antes pelo M2M (com MID Binance no início do bloco)
        pred.resolveBlock(currentCandleClose);

        log.info("[B2B] Resolved block={}: {} threshold={} marketOpen={} closeMid={} -> {} (valid={})",
                predictedBlockUnix, pred.getDirection(), pred.getModelThreshold(),
                pred.getMarketOpen(), currentCandleClose,
                pred.getHit() ? "HIT" : "MISS",
                pred.getValid() != null ? (pred.getValid() ? "VALID" : "INVALID") : "NO_MKT_OPEN");
    }

    private float[] predict(Booster booster, float[] features) {
        try {
            DMatrix mat = new DMatrix(features, 1, features.length, Float.NaN);
            float[][] prob2d = booster.predict(mat);
            if (prob2d == null || prob2d.length == 0) return null;
            return prob2d[0];
        } catch (XGBoostError e) {
            log.error("[B2B] XGBoost predict error: {}", e.getMessage(), e);
            return null;
        }
    }
}
