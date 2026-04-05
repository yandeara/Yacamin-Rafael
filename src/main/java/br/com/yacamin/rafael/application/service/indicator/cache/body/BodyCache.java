package br.com.yacamin.rafael.application.service.indicator.cache.body;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BodyCache implements IndicatorCache {

    private final Map<String, BodyExtension> body = new ConcurrentHashMap<>();
    private final Map<String, BodyAbsExtension> bodyAbs = new ConcurrentHashMap<>();
    private final Map<String, BodyRatioExtension> bodyRatio = new ConcurrentHashMap<>();
    private final Map<String, BodyEnergyExtension> bodyEnergy = new ConcurrentHashMap<>();
    private final Map<String, BodyAbsSmaExtension> bodyAbsSma = new ConcurrentHashMap<>();
    private final Map<String, BodyAbsStdExtension> bodyAbsStd = new ConcurrentHashMap<>();
    private final Map<String, BodyAbsSlopeExtension> bodyAbsSlope = new ConcurrentHashMap<>();
    private final Map<String, BodyRatioSlopeExtension> bodyRatioSlope = new ConcurrentHashMap<>();
    private final Map<String, BodyRatioStdExtension> bodyRatioStd = new ConcurrentHashMap<>();
    private final Map<String, BodySignPersistenceExtension> signPersistence = new ConcurrentHashMap<>();
    private final Map<String, BodyRunLenExtension> runLen = new ConcurrentHashMap<>();

    public BodyExtension getBody(String symbol, CandleIntervals interval, BarSeries series) {
        return body.computeIfAbsent(key(symbol, interval), k -> new BodyExtension(series));
    }

    public BodyAbsExtension getBodyAbs(String symbol, CandleIntervals interval, BarSeries series) {
        return bodyAbs.computeIfAbsent(key(symbol, interval), k ->
                new BodyAbsExtension(getBody(symbol, interval, series)));
    }

    public BodyRatioExtension getBodyRatio(String symbol, CandleIntervals interval, BarSeries series) {
        return bodyRatio.computeIfAbsent(key(symbol, interval), k -> new BodyRatioExtension(series));
    }

    public BodyEnergyExtension getBodyEnergy(String symbol, CandleIntervals interval, BarSeries series) {
        return bodyEnergy.computeIfAbsent(key(symbol, interval), k ->
                new BodyEnergyExtension(getBodyAbs(symbol, interval, series)));
    }

    public BodyAbsSmaExtension getBodyAbsSma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return bodyAbsSma.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new BodyAbsSmaExtension(getBodyAbs(symbol, interval, series), window));
    }

    public BodyAbsStdExtension getBodyAbsStd(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return bodyAbsStd.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new BodyAbsStdExtension(getBodyAbs(symbol, interval, series), window));
    }

    public BodyAbsSlopeExtension getBodyAbsSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return bodyAbsSlope.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new BodyAbsSlopeExtension(getBodyAbs(symbol, interval, series), window));
    }

    public BodyRatioSlopeExtension getBodyRatioSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return bodyRatioSlope.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new BodyRatioSlopeExtension(getBodyRatio(symbol, interval, series), window));
    }

    public BodyRatioStdExtension getBodyRatioStd(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return bodyRatioStd.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new BodyRatioStdExtension(getBodyRatio(symbol, interval, series), window));
    }

    public BodySignPersistenceExtension getSignPersistence(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return signPersistence.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new BodySignPersistenceExtension(getBody(symbol, interval, series), window));
    }

    public BodyRunLenExtension getRunLen(String symbol, CandleIntervals interval, BarSeries series) {
        return runLen.computeIfAbsent(key(symbol, interval), k ->
                new BodyRunLenExtension(getBody(symbol, interval, series), 256));
    }

    public void clear() {
        body.clear();
        bodyAbs.clear();
        bodyRatio.clear();
        bodyEnergy.clear();
        bodyAbsSma.clear();
        bodyAbsStd.clear();
        bodyAbsSlope.clear();
        bodyRatioSlope.clear();
        bodyRatioStd.clear();
        signPersistence.clear();
        runLen.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
