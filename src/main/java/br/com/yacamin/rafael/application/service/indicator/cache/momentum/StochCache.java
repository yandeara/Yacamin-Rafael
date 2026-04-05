package br.com.yacamin.rafael.application.service.indicator.cache.momentum;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StochCache implements IndicatorCache {

    private final Map<String, StochasticOscillatorKIndicator> kCache = new ConcurrentHashMap<>();
    private final Map<String, StochasticOscillatorDIndicator> dCache = new ConcurrentHashMap<>();

    public StochasticOscillatorKIndicator getK(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return kCache.computeIfAbsent(symbol + "_" + interval.name() + "_" + period,
                k -> new StochasticOscillatorKIndicator(series, period));
    }

    public StochasticOscillatorDIndicator getD(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return dCache.computeIfAbsent(symbol + "_" + interval.name() + "_" + period, k -> {
            var stochK = getK(symbol, interval, series, period);
            return new StochasticOscillatorDIndicator(stochK);
        });
    }

    public void clear() {
        kCache.clear();
        dCache.clear();
    }
}
