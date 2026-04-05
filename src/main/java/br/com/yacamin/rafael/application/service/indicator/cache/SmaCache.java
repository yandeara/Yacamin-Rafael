package br.com.yacamin.rafael.application.service.indicator.cache;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.averages.SMAIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SmaCache implements IndicatorCache {

    private final Map<String, SMAIndicator> cache = new ConcurrentHashMap<>();
    private final CloseCache closeCache;

    public SmaCache(CloseCache closeCache) {
        this.closeCache = closeCache;
    }

    public SMAIndicator getSma(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return cache.computeIfAbsent(symbol + "_" + interval.name() + "_" + period, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new SMAIndicator(close, period);
        });
    }

    public void clear() {
        cache.clear();
    }
}
