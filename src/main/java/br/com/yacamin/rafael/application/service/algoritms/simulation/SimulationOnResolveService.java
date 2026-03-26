package br.com.yacamin.rafael.application.service.algoritms.simulation;

import br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response.PolyMarketResolvedEvent;
import br.com.yacamin.rafael.application.service.model.BlockInferenceMemoryService;
import br.com.yacamin.rafael.application.service.model.PredictionEventService;
import br.com.yacamin.rafael.domain.InferencePrediction;
import br.com.yacamin.rafael.domain.Market;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationOnResolveService {

    private final SimulationMarketMemoryService simulationMarketMemoryService;
    private final SimEventPersistenceService simEventPersistenceService;
    private final BlockInferenceMemoryService blockInferenceMemory;
    private final PredictionEventService predictionEventService;

    public void onResolve(String algo, PolyMarketResolvedEvent event) {
        var market = simulationMarketMemoryService.getMarketByAsset(algo, event.getWinningAssetId());
        if (market == null) {
            return;
        }

        var outcome = event.getWinningOutcome();
        log.info("[{}] Resolve: group={}, slug={}, outcome={}", algo, market.getMarketGroup(), market.getSlug(), outcome);
        resolveMarket(algo, market, outcome);
    }

    public void resolveMarketDirect(String algo, Market market, String outcome) {
        log.info("[{}] Resolve (API fallback): group={}, slug={}, outcome={}", algo, market.getMarketGroup(), market.getSlug(), outcome);
        resolveMarket(algo, market, outcome);
    }

    private void resolveMarket(String algo, Market market, String outcome) {
        MDC.put("action", "SIM_RESOLVE");
        MDC.put("algorithm", algo);
        MDC.put("market.slug", market.getSlug());
        MDC.put("market.outcome", outcome);
        try {
            doResolveMarket(algo, market, outcome);
        } finally {
            MDC.remove("action");
            MDC.remove("algorithm");
            MDC.remove("market.outcome");
        }
    }

    private void doResolveMarket(String algo, Market market, String outcome) {
        if (market.isResolved()) {
            return;
        }

        market.setOutcome(outcome);
        market.setResolved(true);

        Map<String, Object> resolvePayload = new LinkedHashMap<>();
        resolvePayload.put("winningOutcome", outcome);
        resolvePayload.put("marketUnixTime", market.getUnixTime());
        simEventPersistenceService.record(algo, market.getMarketGroup(), market.getSlug(), "RESOLVE", resolvePayload);

        // Notificar predictions sobre a resolução do mercado
        long blockUnix = market.getUnixTime();

        InferencePrediction b2b = blockInferenceMemory.getBlockPrediction(blockUnix);
        if (b2b != null && b2b.getHitResolve() == null) {
            b2b.resolveMarket(outcome);
            log.info("[{}] B2B resolve: block={}, direction={}, outcome={}, hitResolve={}",
                    algo, blockUnix, b2b.getDirection(), outcome, b2b.getHitResolve());
            predictionEventService.recordBlockResolved(market.getSlug(), market.getMarketGroup(), b2b);
        }

        InferencePrediction h4 = blockInferenceMemory.getHorizonPrediction(blockUnix);
        if (h4 != null && h4.getHitResolve() == null) {
            h4.resolveMarket(outcome);
            log.info("[{}] H4 resolve: block={}, direction={}, outcome={}, hitResolve={}",
                    algo, blockUnix, h4.getDirection(), outcome, h4.getHitResolve());
            predictionEventService.recordHorizonResolved(market.getSlug(), market.getMarketGroup(), h4);
        }

        log.info("[{}] Resolved: group={}, slug={}, outcome={}", algo, market.getMarketGroup(), market.getSlug(), outcome);
    }
}
