package br.com.yacamin.rafael.application.service.indicator.cache.amihud;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AmihudCache implements IndicatorCache {

    private final CloseCache closeCache;

    private final Map<String, AmihudExtension> raw = new ConcurrentHashMap<>();
    private final Map<String, AmihudSmaExtension> sma = new ConcurrentHashMap<>();
    private final Map<String, AmihudStdExtension> std = new ConcurrentHashMap<>();
    private final Map<String, AmihudZscoreExtension> zscore = new ConcurrentHashMap<>();
    private final Map<String, AmihudSlopeExtension> slope = new ConcurrentHashMap<>();

    public AmihudExtension getAmihudRaw(String symbol, CandleIntervals interval, BarSeries series) {
        return raw.computeIfAbsent(key(symbol, interval), k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new AmihudExtension(close);
        });
    }

    public AmihudSmaExtension getSma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return sma.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new AmihudSmaExtension(getAmihudRaw(symbol, interval, series), window));
    }

    public AmihudStdExtension getStd(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return std.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new AmihudStdExtension(getAmihudRaw(symbol, interval, series), window));
    }

    public AmihudZscoreExtension getZscore(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return zscore.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new AmihudZscoreExtension(getAmihudRaw(symbol, interval, series), window));
    }

    public AmihudSlopeExtension getSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return slope.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new AmihudSlopeExtension(getAmihudRaw(symbol, interval, series), window));
    }

    public void clear() {
        raw.clear();
        sma.clear();
        std.clear();
        zscore.clear();
        slope.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
