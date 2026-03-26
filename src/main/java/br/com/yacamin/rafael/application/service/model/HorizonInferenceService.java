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
 * Lógica HORIZON H4: roda inferência no primeiro candle 1m do bloco para prever o fim do bloco.
 *
 * IMPORTANTE: H4 usa openTime para determinar o bloco e o minuto, não closeTime.
 * O "primeiro candle do bloco" é o candle cujo openTime cai na boundary do bloco.
 *
 * Bloco 15:00-15:05:
 * - Candle openTime=15:00 (closeTime=15:01) → minuto 1, CRIA prediction
 *   - modelThreshold = open candle 15:00 (preço às 15:00)
 *   - Prevê: close do candle openTime=15:04 vs threshold
 * - Candle openTime=15:01 → minuto 2 (ignora)
 * - Candle openTime=15:02 → minuto 3 (ignora)
 * - Candle openTime=15:03 → minuto 4 (ignora)
 * - Candle openTime=15:04 (closeTime=15:05) → minuto 5, RESOLVE prediction
 *   - closeMid = close candle 15:04 (preço às 15:05)
 *   - hit = closeMid vs threshold
 *
 * Sem VALID/INVALID — a previsão está dentro do mesmo bloco.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HorizonInferenceService {

    private static final String DEFAULT_MODEL_KEY = "xgb_BTCUSDT_h4_1m";
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
        log.info("[H4] Live mode: {}", live);
    }

    /**
     * Chamado para cada candle 1m.
     * H4 usa openTime (não closeTime) para determinar bloco e minuto.
     * O primeiro candle do bloco tem openTime na boundary (ex: 15:00).
     */
    public void onCandleClosed(SymbolCandle candle) {
        if (!live) return;
        if (candle.getInterval() != CandleIntervals.I1_MN) return;

        var descriptorOpt = modelRegistryService.getByKey(DEFAULT_MODEL_KEY);
        if (descriptorOpt.isEmpty()) return;

        ModelDescriptor descriptor = descriptorOpt.get();
        if (!candle.getSymbol().equalsIgnoreCase(descriptor.symbol())) return;

        Instant openTime = candle.getOpenTime();

        // H4 usa openTime para bloco (diferente do M2M que usa closeTime)
        long blockUnix = BLOCK_DURATION.currentBlockUnix(openTime);
        long secondsInBlock = openTime.getEpochSecond() - blockUnix;
        int minuteInBlock = (int) (secondsInBlock / 60) + 1; // 1-based

        log.debug("[H4] Candle openTime={} -> block={} min={}", openTime, blockUnix, minuteInBlock);

        // Minuto 5 (openTime = blockUnix + 4*60): RESOLVE
        if (minuteInBlock == 5) {
            resolveHorizonPrediction(blockUnix, candle.getClose(), openTime);
            return;
        }

        // Minuto 1 (openTime = blockUnix): CRIA prediction
        if (minuteInBlock != 1) return;

        if (blockInferenceMemory.getHorizonPrediction(blockUnix) != null) return;

        try {
            runInference(candle, descriptor, blockUnix);
        } catch (Exception e) {
            log.error("[H4] Inference failed for {} @ {}: {}",
                    candle.getSymbol(), openTime, e.getMessage(), e);
        }
    }

    private void runInference(SymbolCandle candle, ModelDescriptor descriptor, long blockUnix) {
        long t0 = System.currentTimeMillis();
        Instant openTime = candle.getOpenTime();

        float[] features = featureExtractorService.extractFeatures(
                candle.getSymbol(), openTime, CandleIntervals.I1_MN);

        if (features == null || features.length == 0) {
            log.warn("[H4] No features for {} @ {}", candle.getSymbol(), openTime);
            return;
        }

        Booster booster = modelRegistryService.getBooster(descriptor.key());
        float[] probs = predict(booster, features);

        if (probs == null || probs.length < 2) {
            log.warn("[H4] Invalid probs for {} @ {}", candle.getSymbol(), openTime);
            return;
        }

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

        StringBuilder probsStr = new StringBuilder("[");
        for (int i = 0; i < probs.length; i++) {
            if (i > 0) probsStr.append(", ");
            probsStr.append(String.format("c%d=%.4f", i, probs[i]));
        }
        probsStr.append("]");

        double modelThreshold = candle.getOpen();
        double openMid = candle.getClose();

        InferencePrediction prediction = new InferencePrediction(
                direction, confidence, 1, openTime, descriptor.key(), openMid);
        prediction.setModelThreshold(modelThreshold);

        blockInferenceMemory.addHorizonPrediction(blockUnix, prediction);

        // Gravar evento PREDICTION_HORIZON
        Market market = simulationMarketMemoryService.findMarketByUnixTime(ALGO, blockUnix);
        String slug = market != null ? market.getSlug() : "unknown-" + blockUnix;
        String mktGroup = market != null ? market.getMarketGroup() : "unknown";
        predictionEventService.recordPrediction(slug, mktGroup, "PREDICTION_HORIZON", prediction);

        Instant targetCandle = openTime.plusSeconds(240); // 4 candles ahead
        long elapsed = System.currentTimeMillis() - t0;
        log.info("[H4] CREATED {} block={} -> {} ({}) threshold={} (open@{}) target=close@openTime={} argmax={} probs={} [{}ms]",
                candle.getSymbol(), blockUnix, direction, confidence,
                modelThreshold, openTime, targetCandle, argmax, probsStr, elapsed);
    }

    private void resolveHorizonPrediction(long blockUnix, double candleClose, Instant openTime) {
        InferencePrediction pred = blockInferenceMemory.getHorizonPrediction(blockUnix);
        if (pred == null || pred.getCloseMid() != null) return;

        pred.resolve(candleClose);

        log.info("[H4] RESOLVED block={}: {} threshold={} closeMid={} (close@openTime={}) -> {}",
                blockUnix, pred.getDirection(), pred.getModelThreshold(),
                candleClose, openTime, pred.getHit() ? "HIT" : "MISS");
    }

    private float[] predict(Booster booster, float[] features) {
        try {
            DMatrix mat = new DMatrix(features, 1, features.length, Float.NaN);
            float[][] prob2d = booster.predict(mat);
            if (prob2d == null || prob2d.length == 0) return null;
            return prob2d[0];
        } catch (XGBoostError e) {
            log.error("[H4] XGBoost predict error: {}", e.getMessage(), e);
            return null;
        }
    }
}
