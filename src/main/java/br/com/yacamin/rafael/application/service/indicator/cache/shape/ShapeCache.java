package br.com.yacamin.rafael.application.service.indicator.cache.shape;
import br.com.yacamin.rafael.application.service.indicator.cache.IndicatorCache;

import br.com.yacamin.rafael.application.service.indicator.cache.shape.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ShapeCache implements IndicatorCache {

    private final Map<String, GeometryScoreExtension> geometryScore = new ConcurrentHashMap<>();
    private final Map<String, CompressionIndexExtension> compressionIndex = new ConcurrentHashMap<>();
    private final Map<String, ShapeIndexExtension> shapeIndex = new ConcurrentHashMap<>();
    private final Map<String, ShapeSmaExtension> geometrySma = new ConcurrentHashMap<>();
    private final Map<String, ShapeStdExtension> geometryStd = new ConcurrentHashMap<>();
    private final Map<String, ShapeSlopeExtension> geometrySlope = new ConcurrentHashMap<>();
    private final Map<String, ShapeSmaExtension> compressionSma = new ConcurrentHashMap<>();
    private final Map<String, ShapeStdExtension> compressionStd = new ConcurrentHashMap<>();
    private final Map<String, ShapeZscoreExtension> compressionZscore = new ConcurrentHashMap<>();
    private final Map<String, ShapeSmaExtension> shapeIndexSma = new ConcurrentHashMap<>();
    private final Map<String, ShapeStdExtension> shapeIndexStd = new ConcurrentHashMap<>();

    public GeometryScoreExtension getGeometryScore(String symbol, CandleIntervals interval, BarSeries series) {
        return geometryScore.computeIfAbsent(key(symbol, interval), k -> new GeometryScoreExtension(series));
    }

    public CompressionIndexExtension getCompressionIndex(String symbol, CandleIntervals interval, BarSeries series) {
        return compressionIndex.computeIfAbsent(key(symbol, interval), k -> new CompressionIndexExtension(series));
    }

    public ShapeIndexExtension getShapeIndex(String symbol, CandleIntervals interval, BarSeries series) {
        return shapeIndex.computeIfAbsent(key(symbol, interval), k -> new ShapeIndexExtension(series));
    }

    public ShapeSmaExtension getGeometrySma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return geometrySma.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new ShapeSmaExtension(getGeometryScore(symbol, interval, series), window));
    }

    public ShapeStdExtension getGeometryStd(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return geometryStd.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new ShapeStdExtension(getGeometryScore(symbol, interval, series), window));
    }

    public ShapeSlopeExtension getGeometrySlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return geometrySlope.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new ShapeSlopeExtension(getGeometryScore(symbol, interval, series), window));
    }

    public ShapeSmaExtension getCompressionSma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return compressionSma.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new ShapeSmaExtension(getCompressionIndex(symbol, interval, series), window));
    }

    public ShapeStdExtension getCompressionStd(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return compressionStd.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new ShapeStdExtension(getCompressionIndex(symbol, interval, series), window));
    }

    public ShapeZscoreExtension getCompressionZscore(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return compressionZscore.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new ShapeZscoreExtension(getCompressionIndex(symbol, interval, series), window));
    }

    public ShapeSmaExtension getShapeIndexSma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return shapeIndexSma.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new ShapeSmaExtension(getShapeIndex(symbol, interval, series), window));
    }

    public ShapeStdExtension getShapeIndexStd(String symbol, CandleIntervals interval, BarSeries series, int window) {
        return shapeIndexStd.computeIfAbsent(key(symbol, interval) + "_" + window, k ->
                new ShapeStdExtension(getShapeIndex(symbol, interval, series), window));
    }

    public void clear() {
        geometryScore.clear();
        compressionIndex.clear();
        shapeIndex.clear();
        geometrySma.clear();
        geometryStd.clear();
        geometrySlope.clear();
        compressionSma.clear();
        compressionStd.clear();
        compressionZscore.clear();
        shapeIndexSma.clear();
        shapeIndexStd.clear();
    }

    private String key(String symbol, CandleIntervals interval) {
        return symbol + "_" + interval.name();
    }
}
