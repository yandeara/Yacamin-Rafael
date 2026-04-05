package br.com.yacamin.rafael.application.service.indicator.cache.momentum;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TsiExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TsiCache implements IndicatorCache {

    private final Map<String, TsiExtension> cache = new ConcurrentHashMap<>();
    private final CloseCache closeCache;

    public TsiCache(CloseCache closeCache) {
        this.closeCache = closeCache;
    }

    public TsiExtension getTsi(String symbol, CandleIntervals interval, BarSeries series,
                               int longPeriod, int shortPeriod) {
        return cache.computeIfAbsent(symbol + "_" + interval.name() + "_" + longPeriod + "_" + shortPeriod, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new TsiExtension(close, longPeriod, shortPeriod);
        });
    }

    public void clear() {
        cache.clear();
    }
}
