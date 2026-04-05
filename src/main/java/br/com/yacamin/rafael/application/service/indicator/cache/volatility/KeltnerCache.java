package br.com.yacamin.rafael.application.service.indicator.cache.volatility;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.AtrCache;
import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.KeltnerLowerExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.KeltnerMiddleExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.KeltnerUpperExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class KeltnerCache implements IndicatorCache {

    private static final double DEFAULT_MULTIPLIER = 1.5;

    private final CloseCache closeCache;
    private final AtrCache atrCache;

    private final Map<String, KeltnerMiddleExtension> middle = new ConcurrentHashMap<>();
    private final Map<String, KeltnerUpperExtension> upper = new ConcurrentHashMap<>();
    private final Map<String, KeltnerLowerExtension> lower = new ConcurrentHashMap<>();

    public KeltnerMiddleExtension getMiddle(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return middle.computeIfAbsent(key(symbol, interval, period), k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new KeltnerMiddleExtension(close, period);
        });
    }

    public KeltnerUpperExtension getUpper(String symbol, CandleIntervals interval, BarSeries series, int emaPeriod, int atrPeriod) {
        return upper.computeIfAbsent(key(symbol, interval, emaPeriod) + "_atr" + atrPeriod, k -> {
            var mid = getMiddle(symbol, interval, series, emaPeriod);
            var atr = atrCache.getAtr14(symbol, interval, series);
            // Use matching ATR period if available
            if (atrPeriod == 14) atr = atrCache.getAtr14(symbol, interval, series);
            else if (atrPeriod == 20) atr = atrCache.getAtr20(symbol, interval, series);
            else if (atrPeriod == 48) atr = atrCache.getAtr48(symbol, interval, series);
            else if (atrPeriod == 96) atr = atrCache.getAtr96(symbol, interval, series);
            return new KeltnerUpperExtension(mid, atr, DEFAULT_MULTIPLIER);
        });
    }

    public KeltnerLowerExtension getLower(String symbol, CandleIntervals interval, BarSeries series, int emaPeriod, int atrPeriod) {
        return lower.computeIfAbsent(key(symbol, interval, emaPeriod) + "_atr" + atrPeriod, k -> {
            var mid = getMiddle(symbol, interval, series, emaPeriod);
            var atr = atrCache.getAtr14(symbol, interval, series);
            if (atrPeriod == 14) atr = atrCache.getAtr14(symbol, interval, series);
            else if (atrPeriod == 20) atr = atrCache.getAtr20(symbol, interval, series);
            else if (atrPeriod == 48) atr = atrCache.getAtr48(symbol, interval, series);
            else if (atrPeriod == 96) atr = atrCache.getAtr96(symbol, interval, series);
            return new KeltnerLowerExtension(mid, atr, DEFAULT_MULTIPLIER);
        });
    }

    public void clear() {
        middle.clear();
        upper.clear();
        lower.clear();
    }

    private String key(String symbol, CandleIntervals interval, int period) {
        return symbol + "_" + interval.name() + "_" + period;
    }
}
