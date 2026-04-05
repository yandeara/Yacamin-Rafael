package br.com.yacamin.rafael.application.service.indicator.cache.trend;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.extension.DeviationIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.averages.EMAIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class EmaCache implements IndicatorCache {

    private final CloseCache closeCache;

    private final Map<String, EMAIndicator> ema = new ConcurrentHashMap<>();
    private final Map<String, LinearRegressionSlopeIndicator> slope = new ConcurrentHashMap<>();
    private final Map<String, DifferenceIndicator> slopeAcc = new ConcurrentHashMap<>();
    private final Map<String, DeviationIndicator> slopeTds = new ConcurrentHashMap<>();

    public EMAIndicator getEma(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return ema.computeIfAbsent(key(symbol, interval) + "_" + period, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new EMAIndicator(close, period);
        });
    }

    public LinearRegressionSlopeIndicator getEmaSlope(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return slope.computeIfAbsent(key(symbol, interval) + "_" + period, k ->
                new LinearRegressionSlopeIndicator(series, getEma(symbol, interval, series, period), period));
    }

    public DifferenceIndicator getEmaSlopeAcc(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return slopeAcc.computeIfAbsent(key(symbol, interval) + "_" + period, k ->
                new DifferenceIndicator(getEmaSlope(symbol, interval, series, period)));
    }

    public DeviationIndicator getEmaSlopeTds(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return slopeTds.computeIfAbsent(key(symbol, interval) + "_" + period, k ->
                new DeviationIndicator(getEmaSlope(symbol, interval, series, period), period));
    }

    public void clear() {
        ema.clear();
        slope.clear();
        slopeAcc.clear();
        slopeTds.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
