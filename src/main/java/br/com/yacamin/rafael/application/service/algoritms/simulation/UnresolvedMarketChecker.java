package br.com.yacamin.rafael.application.service.algoritms.simulation;

import br.com.yacamin.rafael.adapter.out.rest.polymarket.PolymarketGammaMarketsClient;
import br.com.yacamin.rafael.adapter.out.rest.polymarket.dto.PolymarketGetSlugResponse;
import br.com.yacamin.rafael.domain.Market;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnresolvedMarketChecker {

    private static final String ALGO = "RAFAEL";
    private static final long RESOLVE_CHECK_DELAY_SECONDS = 600; // 10 minutos

    private final SimulationMarketMemoryService simulationMarketMemoryService;
    private final SimulationOnResolveService simulationOnResolveService;
    private final PolymarketGammaMarketsClient polymarketGammaMarketsClient;

    @Scheduled(fixedRate = 120_000, initialDelay = 60_000)
    public void checkUnresolvedMarkets() {
        long now = Instant.now().getEpochSecond();
        long cutoff = now - RESOLVE_CHECK_DELAY_SECONDS;

        Map<String, Market> markets = simulationMarketMemoryService.getAllMarkets(ALGO);
        List<Market> unresolved = new ArrayList<>();

        for (Market market : markets.values()) {
            if (market.isResolved()) continue;
            if (market.getUnixTime() == null || market.getUnixTime() > cutoff) continue;
            unresolved.add(market);
        }

        if (unresolved.isEmpty()) return;

        log.info("[OrphanChecker] Found {} unresolved markets to check", unresolved.size());

        for (Market market : unresolved) {
            try {
                String winningOutcome = queryResolution(market.getSlug());
                if (winningOutcome != null) {
                    simulationOnResolveService.resolveMarketDirect(ALGO, market, winningOutcome);
                }
            } catch (Exception e) {
                log.error("[OrphanChecker] Error checking slug {}: {}", market.getSlug(), e.getMessage());
            }
        }
    }

    private String queryResolution(String slug) {
        PolymarketGetSlugResponse response = polymarketGammaMarketsClient.getBySlug(slug);

        if (!Boolean.TRUE.equals(response.getClosed())) {
            return null;
        }

        List<String> outcomePrices = response.getOutcomePrices();
        List<String> outcomes = response.getOutcomes();

        if (outcomePrices == null || outcomes == null || outcomePrices.size() != outcomes.size()) {
            log.warn("[OrphanChecker] Dados incompletos para {}", slug);
            return null;
        }

        for (int i = 0; i < outcomePrices.size(); i++) {
            try {
                double p = Double.parseDouble(outcomePrices.get(i));
                if (p >= 0.9999) {
                    log.info("[OrphanChecker] Resolved via API: {} -> {}", slug, outcomes.get(i));
                    return outcomes.get(i);
                }
            } catch (NumberFormatException ignored) {}
        }

        log.warn("[OrphanChecker] No winning outcome found for {}: prices={}", slug, outcomePrices);
        return null;
    }
}
