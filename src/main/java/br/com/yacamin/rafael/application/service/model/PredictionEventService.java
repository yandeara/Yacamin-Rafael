package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.adapter.out.persistence.RealEventRepository;
import br.com.yacamin.rafael.adapter.out.persistence.SimEventRepository;
import br.com.yacamin.rafael.domain.InferencePrediction;
import br.com.yacamin.rafael.domain.RealEvent;
import br.com.yacamin.rafael.domain.SimEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Grava eventos de predição nas collections `events` (Gabriel) e `sim_events` (Miguel).
 * Ambos com o mesmo payload para que as predições apareçam nas duas timelines.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionEventService {

    private static final String ALGORITHM = "ALPHA";

    private final SimEventRepository simEventRepository;
    private final RealEventRepository realEventRepository;

    // ── PREDICTION CREATED ──

    @Async("mongoWriteExecutor")
    public void recordPrediction(String slug, String marketGroup, String type, InferencePrediction pred) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("marketGroup", marketGroup);
        payload.put("predictionType", type);
        payload.put("direction", pred.getDirection());
        payload.put("confidence", pred.getConfidence());
        payload.put("minuteInBlock", pred.getMinuteInBlock());
        payload.put("modelKey", pred.getModelKey());
        payload.put("openMid", pred.getOpenMid());
        payload.put("modelThreshold", pred.getModelThreshold());

        persist(slug, marketGroup, type, payload);
    }

    // ── M2M RESOLVED (quando candle seguinte fecha) ──

    @Async("mongoWriteExecutor")
    public void recordM2mResolved(String slug, String marketGroup, InferencePrediction pred) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("marketGroup", marketGroup);
        payload.put("predictionType", "PREDICTION_M2M");
        payload.put("direction", pred.getDirection());
        payload.put("confidence", pred.getConfidence());
        payload.put("minuteInBlock", pred.getMinuteInBlock());
        payload.put("modelKey", pred.getModelKey());
        payload.put("modelThreshold", pred.getModelThreshold());
        payload.put("openMid", pred.getOpenMid());
        payload.put("closeMid", pred.getCloseMid());
        payload.put("hit", pred.getHit());

        persist(slug, marketGroup, "PREDICTION_M2M_RESOLVED", payload);
    }

    // ── BLOCK RESOLVED (quando OnResolve da Polymarket chega) ──

    @Async("mongoWriteExecutor")
    public void recordBlockResolved(String slug, String marketGroup, InferencePrediction pred) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("marketGroup", marketGroup);
        payload.put("predictionType", "PREDICTION_BLOCK");
        payload.put("direction", pred.getDirection());
        payload.put("confidence", pred.getConfidence());
        payload.put("modelKey", pred.getModelKey());
        payload.put("modelThreshold", pred.getModelThreshold());
        payload.put("openMid", pred.getOpenMid());
        payload.put("closeMid", pred.getCloseMid());
        payload.put("marketOpen", pred.getMarketOpen());
        payload.put("hitCandle", pred.getHit());
        payload.put("valid", pred.getValid());
        payload.put("resolvedOutcome", pred.getResolvedOutcome());
        payload.put("hitResolve", pred.getHitResolve());

        persist(slug, marketGroup, "PREDICTION_BLOCK_RESOLVED", payload);
    }

    // ── HORIZON RESOLVED (quando OnResolve da Polymarket chega) ──

    @Async("mongoWriteExecutor")
    public void recordHorizonResolved(String slug, String marketGroup, InferencePrediction pred) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("marketGroup", marketGroup);
        payload.put("predictionType", "PREDICTION_HORIZON");
        payload.put("direction", pred.getDirection());
        payload.put("confidence", pred.getConfidence());
        payload.put("modelKey", pred.getModelKey());
        payload.put("modelThreshold", pred.getModelThreshold());
        payload.put("openMid", pred.getOpenMid());
        payload.put("closeMid", pred.getCloseMid());
        payload.put("hitCandle", pred.getHit());
        payload.put("resolvedOutcome", pred.getResolvedOutcome());
        payload.put("hitResolve", pred.getHitResolve());

        persist(slug, marketGroup, "PREDICTION_HORIZON_RESOLVED", payload);
    }

    // ── Core persist: grava nas duas collections ──

    private void persist(String slug, String marketGroup, String type, Map<String, Object> payload) {
        long ts = System.currentTimeMillis();

        try {
            // events (Gabriel) — sem algorithm
            RealEvent realEvent = RealEvent.builder()
                    .slug(slug)
                    .timestamp(ts)
                    .type(type)
                    .payload(payload)
                    .build();
            realEventRepository.save(realEvent);
        } catch (Exception e) {
            log.warn("[PredEvent] FAILED events: type={}, slug={}, error={}", type, slug, e.getMessage());
        }

        try {
            // sim_events (Miguel) — com algorithm
            SimEvent simEvent = SimEvent.builder()
                    .slug(slug)
                    .marketGroup(marketGroup != null ? marketGroup : "unknown")
                    .timestamp(ts)
                    .type(type)
                    .algorithm(ALGORITHM)
                    .payload(payload)
                    .build();
            simEventRepository.save(simEvent);
        } catch (Exception e) {
            log.warn("[PredEvent] FAILED sim_events: type={}, slug={}, error={}", type, slug, e.getMessage());
        }

        log.debug("[PredEvent] Persisted: type={}, slug={}", type, slug);
    }
}
