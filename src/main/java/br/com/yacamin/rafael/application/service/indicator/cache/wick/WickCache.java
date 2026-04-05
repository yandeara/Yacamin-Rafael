package br.com.yacamin.rafael.application.service.indicator.cache.wick;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WickCache implements IndicatorCache {

    private final Map<String, UpperWickExtension> upperWick = new ConcurrentHashMap<>();
    private final Map<String, LowerWickExtension> lowerWick = new ConcurrentHashMap<>();
    private final Map<String, WickImbalanceExtension> wickImbalance = new ConcurrentHashMap<>();
    private final Map<String, ClosePosNormExtension> closePosNorm = new ConcurrentHashMap<>();
    private final Map<String, WickSmaExtension> upperWickSma = new ConcurrentHashMap<>();
    private final Map<String, WickSmaExtension> lowerWickSma = new ConcurrentHashMap<>();
    private final Map<String, WickSlopeExtension> wickImbalanceSlope = new ConcurrentHashMap<>();
    private final Map<String, WickStdExtension> wickImbalanceStd = new ConcurrentHashMap<>();
    private final Map<String, WickSlopeExtension> closePosNormSlope = new ConcurrentHashMap<>();

    public UpperWickExtension getUpperWick(String symbol, CandleIntervals interval, BarSeries series) {
        return upperWick.computeIfAbsent(key(symbol, interval), k -> new UpperWickExtension(series));
    }

    public LowerWickExtension getLowerWick(String symbol, CandleIntervals interval, BarSeries series) {
        return lowerWick.computeIfAbsent(key(symbol, interval), k -> new LowerWickExtension(series));
    }

    public WickImbalanceExtension getWickImbalance(String symbol, CandleIntervals interval, BarSeries series) {
        return wickImbalance.computeIfAbsent(key(symbol, interval), k ->
                new WickImbalanceExtension(getUpperWick(symbol, interval, series), getLowerWick(symbol, interval, series)));
    }

    public ClosePosNormExtension getClosePosNorm(String symbol, CandleIntervals interval, BarSeries series) {
        return closePosNorm.computeIfAbsent(key(symbol, interval), k -> new ClosePosNormExtension(series));
    }

    public WickSmaExtension getUpperWickSma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return upperWickSma.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new WickSmaExtension(getUpperWick(symbol, interval, series), window));
    }

    public WickSmaExtension getLowerWickSma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return lowerWickSma.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new WickSmaExtension(getLowerWick(symbol, interval, series), window));
    }

    public WickSlopeExtension getWickImbalanceSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return wickImbalanceSlope.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new WickSlopeExtension(getWickImbalance(symbol, interval, series), window));
    }

    public WickStdExtension getWickImbalanceStd(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return wickImbalanceStd.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new WickStdExtension(getWickImbalance(symbol, interval, series), window));
    }

    public WickSlopeExtension getClosePosNormSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return closePosNormSlope.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new WickSlopeExtension(getClosePosNorm(symbol, interval, series), window));
    }

    public void clear() {
        upperWick.clear();
        lowerWick.clear();
        wickImbalance.clear();
        closePosNorm.clear();
        upperWickSma.clear();
        lowerWickSma.clear();
        wickImbalanceSlope.clear();
        wickImbalanceStd.clear();
        closePosNormSlope.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
