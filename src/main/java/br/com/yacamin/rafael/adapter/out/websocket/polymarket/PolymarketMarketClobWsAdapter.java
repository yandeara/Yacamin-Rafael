package br.com.yacamin.rafael.adapter.out.websocket.polymarket;

import br.com.yacamin.rafael.adapter.out.rest.polymarket.PolymarketGammaMarketsClient;
import br.com.yacamin.rafael.adapter.out.rest.polymarket.dto.PolymarketGetSlugResponse;
import br.com.yacamin.rafael.application.service.algoritms.simulation.SimulationMarketMemoryService;
import br.com.yacamin.rafael.application.service.trading.MarketGroupService;
import br.com.yacamin.rafael.domain.BlockDuration;
import br.com.yacamin.rafael.domain.Market;
import br.com.yacamin.rafael.domain.MarketGroup;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class PolymarketMarketClobWsAdapter {

    private static final String ALGO = "RAFAEL";

    private final PolymarketMarketClobSocket ws;

    public static final String MARKET_CHANNEL = "market";

    @Value("${polymarket.ws.api-key}")
    private String apiKey;

    @Value("${polymarket.ws.secret}")
    private String secret;

    @Value("${polymarket.ws.pass}")
    private String pass;

    private final PolymarketGammaMarketsClient polymarketGammaMarketsClient;
    private final SimulationMarketMemoryService simulationMarketMemoryService;
    private final MarketGroupService marketGroupService;
    private final Executor marketDiscoveryExecutor;

    private ScheduledExecutorService executor;

    public PolymarketMarketClobWsAdapter(
            PolymarketMarketClobSocket ws,
            PolymarketGammaMarketsClient polymarketGammaMarketsClient,
            SimulationMarketMemoryService simulationMarketMemoryService,
            @Lazy MarketGroupService marketGroupService,
            @Qualifier("marketDiscoveryExecutor") Executor marketDiscoveryExecutor) {
        this.ws = ws;
        this.polymarketGammaMarketsClient = polymarketGammaMarketsClient;
        this.simulationMarketMemoryService = simulationMarketMemoryService;
        this.marketGroupService = marketGroupService;
        this.marketDiscoveryExecutor = marketDiscoveryExecutor;
    }

    public void reconnect() {
        executor.shutdown();
        start();
    }

    public void start() {
        ws.configure(
                MARKET_CHANNEL,
                "wss://ws-subscriptions-clob.polymarket.com",
                List.of("109681959945973300464568698402968596289258214226684818748321941747028805721376"),
                Map.of("apiKey", apiKey,
                        "secret", secret,
                        "passphrase", pass),
                true
        );

        ws.setOnMessage(System.out::println);
        ws.setOnError(Throwable::printStackTrace);

        ws.connect();

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.execute(this::run);

        long initialDelayMs = computeInitialDelayToNextHourMs();

        executor.scheduleAtFixedRate(
                this::run,
                initialDelayMs,
                TimeUnit.HOURS.toMillis(1),
                TimeUnit.MILLISECONDS
        );
    }

    private static long computeInitialDelayToNextHourMs() {
        ZonedDateTime now = ZonedDateTime.now();

        ZonedDateTime nextHour = now
                .truncatedTo(ChronoUnit.HOURS)
                .plusHours(1);

        long delay = Duration.between(now, nextHour).toMillis();

        if (delay <= 0) {
            delay += TimeUnit.HOURS.toMillis(1);
        }

        return delay;
    }

    public void run() {
        for (MarketGroup group : marketGroupService.getActiveGroups()) {
            discoverGroup(group.getSlugPrefix(), group.getBlockDuration());
        }
    }

    public void discoverGroup(String slugPrefix, BlockDuration duration) {
        long nowEpoch = ZonedDateTime.now().toEpochSecond();
        long period = duration.getSeconds();
        int marketsToLoad = (int) Math.ceil(4200.0 / period);

        long currentBoundary = duration.boundaryForEpoch(nowEpoch);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < marketsToLoad; i++) {
            long marketUnix = currentBoundary + (i * period);
            futures.add(CompletableFuture.runAsync(
                    () -> loadMarket(slugPrefix, duration, marketUnix), marketDiscoveryExecutor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void loadMarket(String slugPrefix, BlockDuration duration, long marketUnix) {
        try {
            var slug = slugPrefix + marketUnix;
            PolymarketGetSlugResponse res = polymarketGammaMarketsClient.getBySlug(slug);

            if (simulationMarketMemoryService.getMarket(ALGO, slugPrefix, marketUnix) == null) {
                Market simMarket = buildSimMarket(marketUnix, slug, res, slugPrefix, duration);
                simulationMarketMemoryService.putMarket(ALGO, slugPrefix, marketUnix, simMarket);
                sign(slug, "UP", simMarket.getTokenUp());
                sign(slug, "DOWN", simMarket.getTokenDown());
            } else {
                Market existing = simulationMarketMemoryService.getMarket(ALGO, slugPrefix, marketUnix);
                if (existing.getTokenUp() != null) sign(slug, "UP", existing.getTokenUp());
                if (existing.getTokenDown() != null) sign(slug, "DOWN", existing.getTokenDown());
            }
        } catch (RuntimeException e) {
            log.error("Error loading slug for {}{}", slugPrefix, marketUnix, e);
        }
    }

    private Market buildSimMarket(long marketUnix, String slug, PolymarketGetSlugResponse res,
                                   String marketGroup, BlockDuration duration) {
        Market simMarket = new Market();
        simMarket.setUnixTime(marketUnix);
        simMarket.setSlug(slug);
        simMarket.setTokenUp(res.getClobTokenIds().getFirst());
        simMarket.setTokenDown(res.getClobTokenIds().getLast());
        simMarket.setConditionId(res.getConditionId());
        simMarket.setNegRisk(Boolean.TRUE.equals(res.getNegRisk()));
        simMarket.setTakerBaseFee(res.getTakerBaseFee() != null ? res.getTakerBaseFee() : 0);
        simMarket.setMarketGroup(marketGroup);
        simMarket.setDisplayName(marketGroupService.getDisplayName(marketGroup));
        simMarket.setBlockDuration(duration);
        return simMarket;
    }

    public void sign(String slug, String outcome, String tokenId) {
        ws.subscribeToTokensIds(slug, outcome, tokenId);
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
