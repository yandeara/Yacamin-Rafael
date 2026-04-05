package br.com.yacamin.rafael.application.service.indicator.cache.volatility;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.SmaCache;
import br.com.yacamin.rafael.application.service.indicator.cache.StdCache;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.BollingerWidthExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BollingerCache implements IndicatorCache {

    private final SmaCache smaCache;
    private final StdCache stdCache;

    private final Map<String, BollingerBandsMiddleIndicator> middle = new ConcurrentHashMap<>();
    private final Map<String, BollingerBandsUpperIndicator> upper = new ConcurrentHashMap<>();
    private final Map<String, BollingerBandsLowerIndicator> lower = new ConcurrentHashMap<>();
    private final Map<String, BollingerWidthExtension> width = new ConcurrentHashMap<>();

    public BollingerBandsMiddleIndicator getMiddle(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return middle.computeIfAbsent(key(symbol, interval, period), k -> {
            var sma = smaCache.getSma(symbol, interval, series, period);
            return new BollingerBandsMiddleIndicator(sma);
        });
    }

    public BollingerBandsUpperIndicator getUpper(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return upper.computeIfAbsent(key(symbol, interval, period), k -> {
            var mid = getMiddle(symbol, interval, series, period);
            var std = stdCache.getStd(symbol, interval, series, period);
            return new BollingerBandsUpperIndicator(mid, std);
        });
    }

    public BollingerBandsLowerIndicator getLower(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return lower.computeIfAbsent(key(symbol, interval, period), k -> {
            var mid = getMiddle(symbol, interval, series, period);
            var std = stdCache.getStd(symbol, interval, series, period);
            return new BollingerBandsLowerIndicator(mid, std);
        });
    }

    public BollingerWidthExtension getWidth(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return width.computeIfAbsent(key(symbol, interval, period), k -> {
            var up = getUpper(symbol, interval, series, period);
            var low = getLower(symbol, interval, series, period);
            var mid = getMiddle(symbol, interval, series, period);
            return new BollingerWidthExtension(up, low, mid);
        });
    }

    public void clear() {
        middle.clear();
        upper.clear();
        lower.clear();
        width.clear();
    }

    private String key(String symbol, CandleIntervals interval, int period) {
        return symbol + "_" + interval.name() + "_" + period;
    }
}
