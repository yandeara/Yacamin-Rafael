package br.com.yacamin.rafael.application.service.indicator.cache.trend;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdxCache implements IndicatorCache {

    private final Map<String, ADXIndicator> adx = new ConcurrentHashMap<>();
    private final Map<String, PlusDIIndicator> pdi = new ConcurrentHashMap<>();
    private final Map<String, MinusDIIndicator> mdi = new ConcurrentHashMap<>();

    public ADXIndicator getAdx(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return adx.computeIfAbsent(key(symbol, interval) + "_" + period, k -> new ADXIndicator(series, period));
    }

    public PlusDIIndicator getPdi(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return pdi.computeIfAbsent(key(symbol, interval) + "_" + period, k -> new PlusDIIndicator(series, period));
    }

    public MinusDIIndicator getMdi(String symbol, CandleIntervals interval, BarSeries series, int period) {
        return mdi.computeIfAbsent(key(symbol, interval) + "_" + period, k -> new MinusDIIndicator(series, period));
    }

    public void clear() {
        adx.clear();
        pdi.clear();
        mdi.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
