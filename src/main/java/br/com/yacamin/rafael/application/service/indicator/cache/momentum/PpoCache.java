package br.com.yacamin.rafael.application.service.indicator.cache.momentum;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.PPOIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PpoCache implements IndicatorCache {

    private final Map<String, PPOIndicator> cache = new ConcurrentHashMap<>();
    private final CloseCache closeCache;

    public PpoCache(CloseCache closeCache) {
        this.closeCache = closeCache;
    }

    public PPOIndicator getPpo(String symbol, CandleIntervals interval, BarSeries series,
                               int shortPeriod, int longPeriod) {
        return cache.computeIfAbsent(symbol + "_" + interval.name() + "_" + shortPeriod + "_" + longPeriod, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new PPOIndicator(close, shortPeriod, longPeriod);
        });
    }

    public void clear() {
        cache.clear();
    }
}
