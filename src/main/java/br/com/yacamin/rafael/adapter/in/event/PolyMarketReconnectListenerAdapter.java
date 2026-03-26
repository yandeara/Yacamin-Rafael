package br.com.yacamin.rafael.adapter.in.event;

import br.com.yacamin.rafael.adapter.in.event.dto.PolyMarketReconnectMarketEvent;
import br.com.yacamin.rafael.adapter.out.websocket.polymarket.PolymarketMarketClobWsAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class PolyMarketReconnectListenerAdapter {

    private final PolymarketMarketClobWsAdapter polymarketMarketClobWsAdapter;

    @Async("reconnectPolyMarketExecutor")
    @EventListener
    public void listen(PolyMarketReconnectMarketEvent event) {
        log.info("PolyMarketReconnectMarketEvent Listen");

        polymarketMarketClobWsAdapter.reconnect();
    }

}
