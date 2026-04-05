package br.com.yacamin.rafael.application.service.indicator.cache.kyle;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.OfiCache;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class KyleCache implements IndicatorCache {

    private final CloseCache closeCache;
    private final OfiCache ofiCache;

    private final Map<String, KyleLambdaExtension> lambda = new ConcurrentHashMap<>();
    private final Map<String, KyleSmaExtension> sma = new ConcurrentHashMap<>();
    private final Map<String, KyleStdExtension> std = new ConcurrentHashMap<>();
    private final Map<String, KyleZscoreExtension> zscore = new ConcurrentHashMap<>();
    private final Map<String, KyleSlopeExtension> slope = new ConcurrentHashMap<>();
    private final Map<String, KylePercentileExtension> percentile = new ConcurrentHashMap<>();

    public KyleLambdaExtension getLambda(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return lambda.computeIfAbsent(key(symbol, interval) + "_" + window, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            var ofi = ofiCache.getOfi(symbol, interval, series);
            return new KyleLambdaExtension(close, ofi, window);
        });
    }

    public KyleSmaExtension getSma(String symbol, CandleIntervals interval, BarSeries series,
                                   int lambdaWindow, int smaWindow) {
        return sma.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + smaWindow, k ->
                new KyleSmaExtension(getLambda(symbol, interval, series, lambdaWindow), smaWindow));
    }

    public KyleStdExtension getStd(String symbol, CandleIntervals interval, BarSeries series,
                                   int lambdaWindow, int stdWindow) {
        return std.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + stdWindow, k ->
                new KyleStdExtension(getLambda(symbol, interval, series, lambdaWindow), stdWindow));
    }

    public KyleZscoreExtension getZscore(String symbol, CandleIntervals interval, BarSeries series,
                                         int lambdaWindow, int zscoreWindow) {
        return zscore.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + zscoreWindow, k ->
                new KyleZscoreExtension(getLambda(symbol, interval, series, lambdaWindow), zscoreWindow));
    }

    public KyleSlopeExtension getSlope(String symbol, CandleIntervals interval, BarSeries series,
                                       int lambdaWindow, int slopeWindow) {
        return slope.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + slopeWindow, k ->
                new KyleSlopeExtension(getLambda(symbol, interval, series, lambdaWindow), slopeWindow));
    }

    public KylePercentileExtension getPercentile(String symbol, CandleIntervals interval, BarSeries series,
                                                  int lambdaWindow, int pctWindow) {
        return percentile.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + pctWindow, k ->
                new KylePercentileExtension(getLambda(symbol, interval, series, lambdaWindow), pctWindow));
    }

    public void clear() {
        lambda.clear();
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
