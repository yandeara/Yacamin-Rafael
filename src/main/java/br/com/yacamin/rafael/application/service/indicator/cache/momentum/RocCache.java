package br.com.yacamin.rafael.application.service.indicator.cache.momentum;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ROCIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RocCache implements IndicatorCache {

    private final Map<String, ROCIndicator> cache = new ConcurrentHashMap<>();
    private final CloseCache closeCache;

    public RocCache(CloseCache closeCache) {
        this.closeCache = closeCache;
    }

    public ROCIndicator getRoc(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return cache.computeIfAbsent(symbol + "_" + interval.name() + "_" + period, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new ROCIndicator(close, period);
        });
    }

    public void clear() {
        cache.clear();
    }
}
