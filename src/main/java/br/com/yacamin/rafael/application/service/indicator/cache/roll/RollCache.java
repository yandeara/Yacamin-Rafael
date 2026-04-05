package br.com.yacamin.rafael.application.service.indicator.cache.roll;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.roll.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RollCache implements IndicatorCache {

    private final CloseCache closeCache;

    private final Map<String, RollCovExtension> cov = new ConcurrentHashMap<>();
    private final Map<String, RollCovPctExtension> covPct = new ConcurrentHashMap<>();
    private final Map<String, RollSpreadExtension> spread = new ConcurrentHashMap<>();
    private final Map<String, RollSpreadPctExtension> spreadPct = new ConcurrentHashMap<>();
    private final Map<String, RollSmaExtension> sma = new ConcurrentHashMap<>();
    private final Map<String, RollStdExtension> std = new ConcurrentHashMap<>();
    private final Map<String, RollZscoreExtension> zscore = new ConcurrentHashMap<>();
    private final Map<String, RollSlopeExtension> slope = new ConcurrentHashMap<>();
    private final Map<String, RollPercentileExtension> percentile = new ConcurrentHashMap<>();

    public RollCovExtension getCov(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return cov.computeIfAbsent(key(symbol, interval) + "_" + window, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new RollCovExtension(close, window);
        });
    }

    public RollCovPctExtension getCovPct(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return covPct.computeIfAbsent(key(symbol, interval) + "_" + window, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            return new RollCovPctExtension(close, window);
        });
    }

    public RollSpreadExtension getSpread(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return spread.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new RollSpreadExtension(getCov(symbol, interval, series, window)));
    }

    public RollSpreadPctExtension getSpreadPct(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return spreadPct.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new RollSpreadPctExtension(getCovPct(symbol, interval, series, window)));
    }

    public RollZscoreExtension getCovPctZscore(String symbol, CandleIntervals interval, BarSeries series,
                                                int rollWindow, int zscoreWindow) {
        return zscore.computeIfAbsent(key(symbol, interval) + "_covpct_" + rollWindow + "_" + zscoreWindow, k ->
                new RollZscoreExtension(getCovPct(symbol, interval, series, rollWindow), zscoreWindow));
    }

    public RollZscoreExtension getSpreadPctZscore(String symbol, CandleIntervals interval, BarSeries series,
                                                    int rollWindow, int zscoreWindow) {
        return zscore.computeIfAbsent(key(symbol, interval) + "_spreadpct_" + rollWindow + "_" + zscoreWindow, k ->
                new RollZscoreExtension(getSpreadPct(symbol, interval, series, rollWindow), zscoreWindow));
    }

    public RollSlopeExtension getSpreadPctSlope(String symbol, CandleIntervals interval, BarSeries series,
                                                  int rollWindow, int slopeWindow) {
        return slope.computeIfAbsent(key(symbol, interval) + "_pct_" + rollWindow + "_" + slopeWindow, k ->
                new RollSlopeExtension(getSpreadPct(symbol, interval, series, rollWindow), slopeWindow));
    }

    public RollSmaExtension getSma(String symbol, CandleIntervals interval, BarSeries series,
                                   int rollWindow, int smaWindow) {
        return sma.computeIfAbsent(key(symbol, interval) + "_" + rollWindow + "_" + smaWindow, k ->
                new RollSmaExtension(getSpread(symbol, interval, series, rollWindow), smaWindow));
    }

    public RollStdExtension getStd(String symbol, CandleIntervals interval, BarSeries series,
                                   int rollWindow, int stdWindow) {
        return std.computeIfAbsent(key(symbol, interval) + "_" + rollWindow + "_" + stdWindow, k ->
                new RollStdExtension(getSpread(symbol, interval, series, rollWindow), stdWindow));
    }

    public RollZscoreExtension getZscore(String symbol, CandleIntervals interval, BarSeries series,
                                         int rollWindow, int zscoreWindow) {
        return zscore.computeIfAbsent(key(symbol, interval) + "_" + rollWindow + "_" + zscoreWindow, k ->
                new RollZscoreExtension(getSpread(symbol, interval, series, rollWindow), zscoreWindow));
    }

    public RollZscoreExtension getCovZscore(String symbol, CandleIntervals interval, BarSeries series,
                                            int rollWindow, int zscoreWindow) {
        return zscore.computeIfAbsent(key(symbol, interval) + "_cov_" + rollWindow + "_" + zscoreWindow, k ->
                new RollZscoreExtension(getCov(symbol, interval, series, rollWindow), zscoreWindow));
    }

    public RollSlopeExtension getSlope(String symbol, CandleIntervals interval, BarSeries series,
                                       int rollWindow, int slopeWindow) {
        return slope.computeIfAbsent(key(symbol, interval) + "_" + rollWindow + "_" + slopeWindow, k ->
                new RollSlopeExtension(getSpread(symbol, interval, series, rollWindow), slopeWindow));
    }

    public RollPercentileExtension getPercentile(String symbol, CandleIntervals interval, BarSeries series,
                                                  int rollWindow, int pctWindow) {
        return percentile.computeIfAbsent(key(symbol, interval) + "_" + rollWindow + "_" + pctWindow, k ->
                new RollPercentileExtension(getSpread(symbol, interval, series, rollWindow), pctWindow));
    }

    public void clear() {
        cov.clear();
        covPct.clear();
        spread.clear();
        spreadPct.clear();
        sma.clear();
        std.clear();
        zscore.clear();
        slope.clear();
        percentile.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
