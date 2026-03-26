package br.com.yacamin.rafael.application.service.trading;

import br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response.PolyMarketResolvedEvent;
import br.com.yacamin.rafael.application.service.algoritms.simulation.SimulationMarketMemoryService;
import br.com.yacamin.rafael.application.service.algoritms.simulation.SimulationOnResolveService;
import br.com.yacamin.rafael.domain.Market;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolyDispatchService {

    private static final String ALGO = "RAFAEL";

    private final SimulationOnResolveService simulationOnResolveService;
    private final SimulationMarketMemoryService simulationMarketMemoryService;
    private final MarketGroupService marketGroupService;

    public void dispatchResolve(PolyMarketResolvedEvent event) {
        Market market = simulationMarketMemoryService.getMarketByAsset(ALGO, event.getWinningAssetId());
        if (market == null) return;

        String marketGroup = market.getMarketGroup();
        ExecutorService groupExec = marketGroup != null ? marketGroupService.getExecutor(marketGroup) : null;

        if (groupExec != null) {
            groupExec.submit(() -> simulationOnResolveService.onResolve(ALGO, event));
        } else {
            simulationOnResolveService.onResolve(ALGO, event);
        }
    }
}
