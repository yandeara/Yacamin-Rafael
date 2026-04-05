package br.com.yacamin.rafael.application.service.indicator.cache.hasbrouck;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.CloseCache;
import br.com.yacamin.rafael.application.service.indicator.cache.SvrCache;
import br.com.yacamin.rafael.application.service.indicator.cache.hasbrouck.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class HasbrouckCache implements IndicatorCache {

    private final CloseCache closeCache;
    private final SvrCache svrCache;

    private final Map<String, HasbrouckLambdaExtension> lambda = new ConcurrentHashMap<>();
    private final Map<String, HasbrouckSmaExtension> sma = new ConcurrentHashMap<>();
    private final Map<String, HasbrouckStdExtension> std = new ConcurrentHashMap<>();
    private final Map<String, HasbrouckZscoreExtension> zscore = new ConcurrentHashMap<>();
    private final Map<String, HasbrouckSlopeExtension> slope = new ConcurrentHashMap<>();
    private final Map<String, HasbrouckPercentileExtension> percentile = new ConcurrentHashMap<>();

    public HasbrouckLambdaExtension getLambda(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return lambda.computeIfAbsent(key(symbol, interval) + "_" + window, k -> {
            var close = closeCache.getClosePrice(symbol, interval, series);
            var svr = svrCache.getSvr(symbol, interval, series);
            return new HasbrouckLambdaExtension(close, svr, window);
        });
    }

    public HasbrouckSmaExtension getSma(String symbol, CandleIntervals interval, BarSeries series,
                                        int lambdaWindow, int smaWindow) {
        return sma.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + smaWindow, k ->
                new HasbrouckSmaExtension(getLambda(symbol, interval, series, lambdaWindow), smaWindow));
    }

    public HasbrouckStdExtension getStd(String symbol, CandleIntervals interval, BarSeries series,
                                        int lambdaWindow, int stdWindow) {
        return std.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + stdWindow, k ->
                new HasbrouckStdExtension(getLambda(symbol, interval, series, lambdaWindow), stdWindow));
    }

    public HasbrouckZscoreExtension getZscore(String symbol, CandleIntervals interval, BarSeries series,
                                              int lambdaWindow, int zscoreWindow) {
        return zscore.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + zscoreWindow, k ->
                new HasbrouckZscoreExtension(getLambda(symbol, interval, series, lambdaWindow), zscoreWindow));
    }

    public HasbrouckSlopeExtension getSlope(String symbol, CandleIntervals interval, BarSeries series,
                                            int lambdaWindow, int slopeWindow) {
        return slope.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + slopeWindow, k ->
                new HasbrouckSlopeExtension(getLambda(symbol, interval, series, lambdaWindow), slopeWindow));
    }

    public HasbrouckPercentileExtension getPercentile(String symbol, CandleIntervals interval, BarSeries series,
                                                      int lambdaWindow, int pctWindow) {
        return percentile.computeIfAbsent(key(symbol, interval) + "_" + lambdaWindow + "_" + pctWindow, k ->
                new HasbrouckPercentileExtension(getLambda(symbol, interval, series, lambdaWindow), pctWindow));
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
