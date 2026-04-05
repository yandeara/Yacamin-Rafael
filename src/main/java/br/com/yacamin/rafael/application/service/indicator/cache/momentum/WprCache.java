package br.com.yacamin.rafael.application.service.indicator.cache.momentum;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.WilliamsRIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WprCache implements IndicatorCache {

    private final Map<String, WilliamsRIndicator> cache = new ConcurrentHashMap<>();

    public WilliamsRIndicator getWpr(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return cache.computeIfAbsent(symbol + "_" + interval.name() + "_" + period,
                k -> new WilliamsRIndicator(series, period));
    }

    public void clear() {
        cache.clear();
    }
}
