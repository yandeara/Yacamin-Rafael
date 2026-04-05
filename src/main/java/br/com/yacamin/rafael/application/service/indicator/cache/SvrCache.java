package br.com.yacamin.rafael.application.service.indicator.cache;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.extension.SvrExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SvrCache implements IndicatorCache {

    private final Map<String, SvrExtension> svr = new ConcurrentHashMap<>();

    public SvrExtension getSvr(String symbol, CandleIntervals interval, BarSeries series) {
        return svr.computeIfAbsent(key(symbol, interval), k -> new SvrExtension(series));
    }

    public void clear() {
        svr.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
