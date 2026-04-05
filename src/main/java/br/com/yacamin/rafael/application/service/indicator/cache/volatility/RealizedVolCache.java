package br.com.yacamin.rafael.application.service.indicator.cache.volatility;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.RealizedVolExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RealizedVolCache implements IndicatorCache {

    private final CloseCache closeCache;

    private final Map<String, RealizedVolExtension> rv = new ConcurrentHashMap<>();

    public RealizedVolExtension getRv(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return rv.computeIfAbsent(symbol + "_" + interval.name() + "_" + period, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new RealizedVolExtension(close, period);
        });
    }

    public void clear() {
        rv.clear();
    }
}
