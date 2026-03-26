package br.com.yacamin.rafael.adapter.in.event;

import br.com.yacamin.rafael.adapter.in.event.dto.ReconnectMarketDataSocketEvent;
import br.com.yacamin.rafael.adapter.out.websocket.binance.SpotMarketDataWsAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class ReconnectMarketDataSocketListenerAdapter {

    private final SpotMarketDataWsAdapter spotMarketDataWsAdapter;

    @Async("reconnectMarketDataSocketExecutor")
    @EventListener
    public void listen(ReconnectMarketDataSocketEvent event) {
        log.info("ReconnectMarketDataSocketExecutor Listen");

        if(event.getType().equals("SPOT")) {
            spotMarketDataWsAdapter.reconnect();
        }
    }

}
