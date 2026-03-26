package br.com.yacamin.rafael.application.service.algoritms.simulation;

import br.com.yacamin.rafael.application.service.trading.MarketGroupService;
import br.com.yacamin.rafael.domain.BlockDuration;
import br.com.yacamin.rafael.domain.Market;
import br.com.yacamin.rafael.domain.PolyAsset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SimulationMarketMemoryService {

    private final MarketGroupService marketGroupService;

    /**
     * Main market storage: algoName -> compositeKey("marketGroup|unixTime") -> Market
     */
    private final Map<String, Map<String, Market>> marketsByAlgo = new ConcurrentHashMap<>();
    private final Map<String, Map<String, PolyAsset>> assetsByAlgo = new ConcurrentHashMap<>();
    /** assetId -> compositeKey for lookup */
    private final Map<String, Map<String, String>> marketKeyByAssetByAlgo = new ConcurrentHashMap<>();

    public SimulationMarketMemoryService(@Lazy MarketGroupService marketGroupService) {
        this.marketGroupService = marketGroupService;
    }

    public static String marketKey(String marketGroup, long unixTime) {
        return marketGroup + "|" + unixTime;
    }

    private Map<String, Market> marketsFor(String algoName) {
        return marketsByAlgo.computeIfAbsent(algoName, k -> new ConcurrentHashMap<>());
    }

    private Map<String, PolyAsset> assetsFor(String algoName) {
        return assetsByAlgo.computeIfAbsent(algoName, k -> new ConcurrentHashMap<>());
    }

    private Map<String, String> marketKeyByAssetFor(String algoName) {
        return marketKeyByAssetByAlgo.computeIfAbsent(algoName, k -> new ConcurrentHashMap<>());
    }

    public Market getMarket(String algoName, String marketGroup, long blockUnix) {
        return marketsFor(algoName).get(marketKey(marketGroup, blockUnix));
    }

    public Map<String, Market> getAllMarkets(String algoName) {
        return marketsFor(algoName);
    }

    public Market findMarketByUnixTime(String algoName, long unixTime) {
        for (Market m : marketsFor(algoName).values()) {
            if (m.getUnixTime() != null && m.getUnixTime() == unixTime) {
                return m;
            }
        }
        return null;
    }

    public Market getMarketByAsset(String algoName, String assetId) {
        String key = marketKeyByAssetFor(algoName).get(assetId);
        if (key == null) return null;
        return marketsFor(algoName).get(key);
    }

    public PolyAsset getAsset(String algoName, String asset) {
        return assetsFor(algoName).get(asset);
    }

    public void putMarket(String algoName, String marketGroup, long unixTime, Market market) {
        String key = marketKey(marketGroup, unixTime);
        marketsFor(algoName).put(key, market);

        PolyAsset assetUp = new PolyAsset();
        assetUp.setUnixTime(market.getUnixTime());
        assetUp.setSide("UP");
        assetsFor(algoName).put(market.getTokenUp(), assetUp);
        marketKeyByAssetFor(algoName).put(market.getTokenUp(), key);

        PolyAsset assetDown = new PolyAsset();
        assetDown.setUnixTime(market.getUnixTime());
        assetDown.setSide("DOWN");
        assetsFor(algoName).put(market.getTokenDown(), assetDown);
        marketKeyByAssetFor(algoName).put(market.getTokenDown(), key);
    }

    @Scheduled(fixedRate = 1_000)
    public void updateTimeRemaining() {
        long now = Instant.now().getEpochSecond();

        for (Map<String, Market> markets : marketsByAlgo.values()) {
            for (Market market : markets.values()) {
                if (market.getUnixTime() > now) continue;
                BlockDuration duration = market.getBlockDuration() != null
                        ? market.getBlockDuration()
                        : marketGroupService.getBlockDuration(market.getMarketGroup());
                long end = duration.blockEnd(market.getUnixTime());
                market.setTimeRemaining(end - now);
            }
        }
    }

    @Scheduled(fixedRate = 900_000, initialDelay = 900_000)
    public void cleanupOldMarkets() {
        long cutoff = Instant.now().getEpochSecond() - 7200;

        for (Map.Entry<String, Map<String, Market>> algoEntry : marketsByAlgo.entrySet()) {
            String algoName = algoEntry.getKey();
            Map<String, Market> markets = algoEntry.getValue();
            Map<String, PolyAsset> assets = assetsFor(algoName);
            Map<String, String> marketKeyByAsset = marketKeyByAssetFor(algoName);

            int removed = 0;
            Iterator<Map.Entry<String, Market>> it = markets.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Market> entry = it.next();
                Market market = entry.getValue();
                if (market.getUnixTime() < cutoff) {
                    assets.remove(market.getTokenUp());
                    assets.remove(market.getTokenDown());
                    marketKeyByAsset.remove(market.getTokenUp());
                    marketKeyByAsset.remove(market.getTokenDown());
                    it.remove();
                    removed++;
                }
            }

            if (removed > 0) {
                log.info("Cleanup: removed {} old markets from {} simulation memory", removed, algoName);
            }
        }
    }

    public Set<String> getAllActiveAssets() {
        // Return assets from first algo (all algos share the same token IDs)
        for (Map<String, PolyAsset> assets : assetsByAlgo.values()) {
            return assets.keySet();
        }
        return Collections.emptySet();
    }

    public Set<String> getAlgoNames() {
        return marketsByAlgo.keySet();
    }
}
