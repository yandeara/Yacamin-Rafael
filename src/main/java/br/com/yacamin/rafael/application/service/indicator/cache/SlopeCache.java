package br.com.yacamin.rafael.application.service.indicator.cache;

import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SlopeCache implements IndicatorCache {

    private final Map<String, LinearRegressionSlopeIndicator> slopeCache = new ConcurrentHashMap<>();
    private final Map<String, DifferenceIndicator> accCache = new ConcurrentHashMap<>();

    public LinearRegressionSlopeIndicator getSlope(String symbol, CandleIntervals interval, BarSeries series,
                                                    Indicator<Num> base, String baseName, int period) {
        String key = symbol + "_" + interval.name() + "_" + baseName + "_" + period;
        return slopeCache.computeIfAbsent(key, k -> new LinearRegressionSlopeIndicator(series, base, period));
    }

    public DifferenceIndicator getAcceleration(String symbol, CandleIntervals interval, BarSeries series,
                                                Indicator<Num> base, String baseName, int period) {
        String key = symbol + "_" + interval.name() + "_acc_" + baseName + "_" + period;
        return accCache.computeIfAbsent(key, k -> {
            var slope = getSlope(symbol, interval, series, base, baseName, period);
            return new DifferenceIndicator(slope);
        });
    }

    public void clear() {
        slopeCache.clear();
        accCache.clear();
    }
}
