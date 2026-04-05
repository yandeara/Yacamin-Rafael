package br.com.yacamin.rafael.application.service.indicator.cache;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.extension.VwapExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VwapCache implements IndicatorCache {

    private final Map<String, VwapExtension> vwap = new ConcurrentHashMap<>();

    public VwapExtension getVwap(String symbol, CandleIntervals interval, BarSeries series) {
        return vwap.computeIfAbsent(key(symbol, interval), k -> new VwapExtension(series));
    }

    public void clear() {
        vwap.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
