package br.com.yacamin.rafael.adapter.in.event;

import br.com.yacamin.rafael.adapter.in.event.dto.BookTickerUpdateSocketEvent;
import br.com.yacamin.rafael.application.service.trading.TickDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceListenerAdapter {

    private final TickDispatchService tickDispatchService;

    @Async("bookTickUpdateListenerExecutor")
    @EventListener
    public void onBookTick(BookTickerUpdateSocketEvent event) {
        MDC.put("event.type", "BOOK_TICK");
        try {
            tickDispatchService.dispatch(event.getResponse());
        } finally {
            MDC.clear();
        }
    }
}
