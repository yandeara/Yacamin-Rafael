package br.com.yacamin.rafael.application.service.indicator.cache.momentum;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TrixExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TrixCache implements IndicatorCache {

    private final Map<String, TrixExtension> cache = new ConcurrentHashMap<>();
    private final CloseCache closeCache;

    public TrixCache(CloseCache closeCache) {
        this.closeCache = closeCache;
    }

    public TrixExtension getTrix(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return cache.computeIfAbsent(symbol + "_" + interval.name() + "_" + period, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new TrixExtension(close, period);
        });
    }

    public void clear() {
        cache.clear();
    }
}
