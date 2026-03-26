package br.com.yacamin.rafael.adapter.in.event;

import br.com.yacamin.rafael.adapter.out.websocket.polymarket.dto.response.PolyMarketResolvedEvent;
import br.com.yacamin.rafael.application.service.trading.PolyDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolyMarketWsMarketListener {

    private final PolyDispatchService polyDispatchService;

    @Async("polyOnResolveExecutor")
    @EventListener
    public void onMarketResolve(PolyMarketResolvedEvent event) {
        MDC.put("event.type", "MARKET_RESOLVE");
        MDC.put("market.winningAssetId", event.getWinningAssetId());
        try {
            polyDispatchService.dispatchResolve(event);
        } finally {
            MDC.clear();
        }
    }
}
