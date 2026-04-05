package br.com.yacamin.rafael.application.service.indicator.cache;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AtrCache implements IndicatorCache {

    private final Map<String, ATRIndicator> cache = new ConcurrentHashMap<>();

    public ATRIndicator getAtr14(String symbol, CandleIntervals interval, BarSeries series) {
        return getAtr(symbol, interval, series, 14);
    }

    public ATRIndicator getAtr20(String symbol, CandleIntervals interval, BarSeries series) {
        return getAtr(symbol, interval, series, 20);
    }

    public ATRIndicator getAtr48(String symbol, CandleIntervals interval, BarSeries series) {
        return getAtr(symbol, interval, series, 48);
    }

    public ATRIndicator getAtr96(String symbol, CandleIntervals interval, BarSeries series) {
        return getAtr(symbol, interval, series, 96);
    }

    public ATRIndicator getAtr(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return cache.computeIfAbsent(symbol + "_" + interval.name() + "_" + period,
                k -> new ATRIndicator(series, period));
    }

    public void clear() {
        cache.clear();
    }
}
