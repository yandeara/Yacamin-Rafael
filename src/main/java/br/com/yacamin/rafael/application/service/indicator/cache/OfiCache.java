package br.com.yacamin.rafael.application.service.indicator.cache;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.extension.OfiExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OfiCache implements IndicatorCache {

    private final Map<String, OfiExtension> ofi = new ConcurrentHashMap<>();

    public OfiExtension getOfi(String symbol, CandleIntervals interval, BarSeries series) {
        return ofi.computeIfAbsent(key(symbol, interval), k -> new OfiExtension(series));
    }

    public void clear() {
        ofi.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
