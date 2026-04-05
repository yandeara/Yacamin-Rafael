package br.com.yacamin.rafael.application.service.indicator.cache.range;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RangeCache implements IndicatorCache {

    private final Map<String, RangeExtension> range = new ConcurrentHashMap<>();
    private final Map<String, TrueRangeExtension> trueRange = new ConcurrentHashMap<>();
    private final Map<String, Hlc3Extension> hlc3 = new ConcurrentHashMap<>();
    private final Map<String, LogRangeExtension> logRange = new ConcurrentHashMap<>();
    private final Map<String, RangeSmaExtension> rangeSma = new ConcurrentHashMap<>();
    private final Map<String, RangeStdExtension> rangeStd = new ConcurrentHashMap<>();
    private final Map<String, RangeSlopeExtension> rangeSlope = new ConcurrentHashMap<>();
    private final Map<String, RangeSmaExtension> hlc3Sma = new ConcurrentHashMap<>();
    private final Map<String, RangeStdExtension> hlc3Std = new ConcurrentHashMap<>();
    private final Map<String, RangeSlopeExtension> hlc3Slope = new ConcurrentHashMap<>();
    private final Map<String, RangeSmaExtension> logRangeSma = new ConcurrentHashMap<>();
    private final Map<String, RangeStdExtension> logRangeStd = new ConcurrentHashMap<>();
    private final Map<String, RangeSlopeExtension> logRangeSlope = new ConcurrentHashMap<>();
    private final Map<String, RangePercentileExtension> logRangePercentile = new ConcurrentHashMap<>();
    private final Map<String, RangeLaggedMeanExtension> rangeLaggedMean = new ConcurrentHashMap<>();

    // --- Base indicators ---

    public RangeExtension getRange(String symbol, CandleIntervals interval, BarSeries series) {
        return range.computeIfAbsent(key(symbol, interval), k -> new RangeExtension(series));
    }

    public TrueRangeExtension getTrueRange(String symbol, CandleIntervals interval, BarSeries series) {
        return trueRange.computeIfAbsent(key(symbol, interval), k -> new TrueRangeExtension(series));
    }

    public Hlc3Extension getHlc3(String symbol, CandleIntervals interval, BarSeries series) {
        return hlc3.computeIfAbsent(key(symbol, interval), k -> new Hlc3Extension(series));
    }

    public LogRangeExtension getLogRange(String symbol, CandleIntervals interval, BarSeries series) {
        return logRange.computeIfAbsent(key(symbol, interval), k ->
                new LogRangeExtension(getRange(symbol, interval, series)));
    }

    // --- Range derivatives ---

    public RangeSmaExtension getRangeSma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return rangeSma.computeIfAbsent(key(symbol, interval) + "_range_" + window, k ->
                new RangeSmaExtension(getRange(symbol, interval, series), window));
    }

    public RangeStdExtension getRangeStd(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return rangeStd.computeIfAbsent(key(symbol, interval) + "_range_" + window, k ->
                new RangeStdExtension(getRange(symbol, interval, series), window));
    }

    public RangeSlopeExtension getRangeSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return rangeSlope.computeIfAbsent(key(symbol, interval) + "_range_" + window, k ->
                new RangeSlopeExtension(getRange(symbol, interval, series), window));
    }

    // --- HLC3 derivatives ---

    public RangeSmaExtension getHlc3Sma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return hlc3Sma.computeIfAbsent(key(symbol, interval) + "_hlc3_" + window, k ->
                new RangeSmaExtension(getHlc3(symbol, interval, series), window));
    }

    public RangeStdExtension getHlc3Std(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return hlc3Std.computeIfAbsent(key(symbol, interval) + "_hlc3_" + window, k ->
                new RangeStdExtension(getHlc3(symbol, interval, series), window));
    }

    public RangeSlopeExtension getHlc3Slope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return hlc3Slope.computeIfAbsent(key(symbol, interval) + "_hlc3_" + window, k ->
                new RangeSlopeExtension(getHlc3(symbol, interval, series), window));
    }

    // --- LogRange derivatives ---

    public RangeSmaExtension getLogRangeSma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return logRangeSma.computeIfAbsent(key(symbol, interval) + "_logRange_" + window, k ->
                new RangeSmaExtension(getLogRange(symbol, interval, series), window));
    }

    public RangeStdExtension getLogRangeStd(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return logRangeStd.computeIfAbsent(key(symbol, interval) + "_logRange_" + window, k ->
                new RangeStdExtension(getLogRange(symbol, interval, series), window));
    }

    public RangeSlopeExtension getLogRangeSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return logRangeSlope.computeIfAbsent(key(symbol, interval) + "_logRange_" + window, k ->
                new RangeSlopeExtension(getLogRange(symbol, interval, series), window));
    }

    public RangePercentileExtension getLogRangePercentile(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return logRangePercentile.computeIfAbsent(key(symbol, interval) + "_logRange_" + window, k ->
                new RangePercentileExtension(getLogRange(symbol, interval, series), window));
    }

    // --- Range lagged mean ---

    public RangeLaggedMeanExtension getRangeLaggedMean(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return rangeLaggedMean.computeIfAbsent(key(symbol, interval) + "_range_" + window, k ->
                new RangeLaggedMeanExtension(getRange(symbol, interval, series), window));
    }

    public void clear() {
        range.clear();
        trueRange.clear();
        hlc3.clear();
        logRange.clear();
        rangeSma.clear();
        rangeStd.clear();
        rangeSlope.clear();
        hlc3Sma.clear();
        hlc3Std.clear();
        hlc3Slope.clear();
        logRangeSma.clear();
        logRangeStd.clear();
        logRangeSlope.clear();
        logRangePercentile.clear();
        rangeLaggedMean.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
