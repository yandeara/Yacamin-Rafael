package br.com.yacamin.rafael.application.service.indicator.cache.momentum;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CMOIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CmoCache implements IndicatorCache {

    private final Map<String, CMOIndicator> cache = new ConcurrentHashMap<>();
    private final CloseCache closeCache;

    public CmoCache(CloseCache closeCache) {
        this.closeCache = closeCache;
    }

    public CMOIndicator getCmo(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return cache.computeIfAbsent(symbol + "_" + interval.name() + "_" + period, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new CMOIndicator(close, period);
        });
    }

    public void clear() {
        cache.clear();
    }
}
