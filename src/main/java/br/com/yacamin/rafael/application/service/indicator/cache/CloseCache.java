package br.com.yacamin.rafael.application.service.indicator.cache;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CloseCache implements IndicatorCache {

    private final Map<String, ClosePriceIndicator> closePrice = new ConcurrentHashMap<>();

    public ClosePriceIndicator getClosePrice(String symbol, CandleIntervals interval, BarSeries series) {
        return closePrice.computeIfAbsent(key(symbol, interval), k -> new ClosePriceIndicator(series));
    }

    public void clear() {
        closePrice.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
