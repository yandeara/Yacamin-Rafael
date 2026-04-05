package br.com.yacamin.rafael.application.service.indicator.cache.volatility;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.DonchianLowerExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.DonchianMiddleExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.DonchianUpperExtension;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DonchianCache implements IndicatorCache {

    private final Map<String, DonchianUpperExtension> upper = new ConcurrentHashMap<>();
    private final Map<String, DonchianLowerExtension> lower = new ConcurrentHashMap<>();
    private final Map<String, DonchianMiddleExtension> middle = new ConcurrentHashMap<>();

    public DonchianUpperExtension getUpper(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return upper.computeIfAbsent(key(symbol, interval, period), k -> new DonchianUpperExtension(series, period));
    }

    public DonchianLowerExtension getLower(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return lower.computeIfAbsent(key(symbol, interval, period), k -> new DonchianLowerExtension(series, period));
    }

    public DonchianMiddleExtension getMiddle(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return middle.computeIfAbsent(key(symbol, interval, period), k -> {
            var up = getUpper(symbol, interval, series, period);
            var low = getLower(symbol, interval, series, period);
            return new DonchianMiddleExtension(up, low);
        });
    }

    public void clear() {
        upper.clear();
        lower.clear();
        middle.clear();
    }

    private String key(String symbol, CandleIntervals interval, int period) {
        return symbol + "_" + interval.name() + "_" + period;
    }
}
