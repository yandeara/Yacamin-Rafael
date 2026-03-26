package br.com.yacamin.rafael.adapter.in.event;

import br.com.yacamin.rafael.adapter.in.event.dto.KlineUpdateSocketEvent;
import br.com.yacamin.rafael.adapter.out.persistence.Candle1MnRepository;
import br.com.yacamin.rafael.adapter.out.persistence.Candle5MnRepository;
import br.com.yacamin.rafael.application.service.candle.BarSeriesCacheService;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KlineListenerAdapter {

    private final BarSeriesCacheService barSeriesCacheService;
    private final Candle1MnRepository candle1MnRepository;
    private final Candle5MnRepository candle5MnRepository;

    @Async("klineUpdateListenerExecutor")
    @EventListener
    public void listen(KlineUpdateSocketEvent event) {
        var data = event.response().kline();

        if (!data.closed()) {
            return;
        }

        try {
            long t0 = System.currentTimeMillis();
            SymbolCandle candle = SymbolCandle.fromKlineData(data);
            CandleIntervals interval = CandleIntervals.valueOfLabel(data.interval());

            log.info("[KLINE] Closed candle received: {} [{}] @ {} | O={} H={} L={} C={} V={}",
                    candle.getSymbol(), interval, candle.getOpenTime(),
                    candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(), candle.getVolume());

            if (interval == CandleIntervals.I1_MN) {
                candle1MnRepository.save(candle.toCandle1Mn());
                log.debug("[KLINE] Persisted 1m to Scylla: {} @ {}", candle.getSymbol(), candle.getOpenTime());
            } else if (interval == CandleIntervals.I5_MN) {
                candle5MnRepository.save(candle.toCandle5Mn());
                log.debug("[KLINE] Persisted 5m to Scylla: {} @ {}", candle.getSymbol(), candle.getOpenTime());
            }

            barSeriesCacheService.update(candle.getSymbol(), interval, candle, false);

            log.info("[KLINE] Processed in {}ms: {} [{}] @ {}",
                    System.currentTimeMillis() - t0, candle.getSymbol(), interval, candle.getOpenTime());
        } catch (Exception e) {
            log.error("[KLINE] ERROR processing event: {}", e.getMessage(), e);
        }
    }
}
