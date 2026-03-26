package br.com.yacamin.rafael.adapter.in.event;

import br.com.yacamin.rafael.adapter.in.event.dto.SubMessageMarketDataSocketEvent;
import br.com.yacamin.rafael.adapter.out.websocket.binance.SpotMarketDataWsAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SubMessageMarketDataSocketListenerAdapter {

    private final SpotMarketDataWsAdapter spotMarketDataWsAdapter;

    @Async("reconnectMarketDataSocketExecutor")
    @EventListener
    public void listen(SubMessageMarketDataSocketEvent event) {
        log.info("ReconnectMarketDataSocketExecutor Listen");

        if(event.getType().equals("SPOT")) {
            spotMarketDataWsAdapter.manageSubMessage(event.getMessage());
        }
    }
}
