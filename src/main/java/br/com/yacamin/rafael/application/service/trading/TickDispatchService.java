package br.com.yacamin.rafael.application.service.trading;

import br.com.yacamin.rafael.adapter.out.websocket.binance.dto.response.BookTickerUpdateResponse;
import br.com.yacamin.rafael.application.service.algoritms.simulation.SimulationMarketMemoryService;
import br.com.yacamin.rafael.domain.Market;
import br.com.yacamin.rafael.domain.MarketGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class TickDispatchService {

    private final SimulationMarketMemoryService simulationMarketMemoryService;
    private final MarketGroupService marketGroupService;

    /** Latest-only: guarda apenas o tick mais recente por grupo, descarta intermediarios */
    private final Map<String, AtomicReference<BookTickerUpdateResponse>> latestTicks = new ConcurrentHashMap<>();

    public void dispatch(BookTickerUpdateResponse tick) {
        String symbol = tick.getSymbol();
        if (symbol == null || symbol.isBlank()) {
            return;
        }

        String stream = symbol + "@bookTicker";
        Set<String> groupPrefixes = marketGroupService.getGroupsForStream(stream);

        if (groupPrefixes.isEmpty()) return;

        for (String slugPrefix : groupPrefixes) {
            MarketGroup group = marketGroupService.getActiveGroup(slugPrefix);
            if (group == null) continue;

            ExecutorService groupExec = marketGroupService.getExecutor(slugPrefix);
            if (groupExec == null) continue;

            AtomicReference<BookTickerUpdateResponse> ref =
                    latestTicks.computeIfAbsent(slugPrefix, k -> new AtomicReference<>());

            BookTickerUpdateResponse prev = ref.getAndSet(tick);
            if (prev == null) {
                groupExec.execute(() -> {
                    BookTickerUpdateResponse latest = ref.getAndSet(null);
                    if (latest != null) {
                        processTickForGroup(latest, group);
                    }
                });
            }
        }
    }

    private void processTickForGroup(BookTickerUpdateResponse tick, MarketGroup group) {
        long blockUnix = group.getBlockDuration().currentBlockUnix();

        Market m = simulationMarketMemoryService.getMarket(
                "RAFAEL", group.getSlugPrefix(), blockUnix);
        if (m != null) {
            double mid = (tick.getBestBid() + tick.getBestAsk()) / 2.0;
            m.setMidPrice(mid);
            if (m.getOpenPrice() == 0) {
                m.setOpenPrice(mid);
            }
            m.setTickCount(m.getTickCount() + 1);
        }
    }
}
