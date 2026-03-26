package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.microstructure.BodyCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Service
@RequiredArgsConstructor
public class BodyDerivation {

    private static final double EPS = 1e-12;

    private final BodyCacheService bodyCacheService;

    // =========================================================================
    // 1) BODY RAW
    // =========================================================================

    public double calculateBody(SymbolCandle c, BarSeries series, int index) {
        return bodyCacheService
                .getBody(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    public double calculateBodyAbs(SymbolCandle c, BarSeries series, int index) {
        return bodyCacheService
                .getBodyAbs(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    public double calculateBodyRatio(SymbolCandle c, BarSeries series, int index) {
        return bodyCacheService
                .getBodyRatio(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    public double calculateBodyEnergyRaw(SymbolCandle c, BarSeries series, int index) {
        return bodyCacheService
                .getBodyEnergy(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 2) BODY DYNAMICS (janela) — agora via Indicators
    // =========================================================================

    public double calculateBodySlope(SymbolCandle c, BarSeries series, int index, int window) {
        return bodyCacheService
                .getBodyAbsSlope(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    public double calculateBodyMa(SymbolCandle c, BarSeries series, int index, int window) {
        return bodyCacheService
                .getBodyAbsMa(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    public double calculateBodyVol(SymbolCandle c, BarSeries series, int index, int window) {
        return bodyCacheService
                .getBodyAbsVol(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 3) BODY RATIO DYNAMICS — via Indicators
    // =========================================================================

    public double calculateBodyRatioSlope(SymbolCandle c, BarSeries series, int index, int window) {
        return bodyCacheService
                .getBodyRatioSlope(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    public double calculateBodyRatioVol(SymbolCandle c, BarSeries series, int index, int window) {
        return bodyCacheService
                .getBodyRatioVol(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 4) NORMALIZED / FINAL FEATURES
    // =========================================================================

    public double calculateBodyAtrRatio(ATRIndicator atr, SymbolCandle c, BarSeries series, int index) {
        double atrV = atr.getValue(index).doubleValue();
        if (atrV < EPS) return 0.0;

        double bodyAbs = calculateBodyAbs(c, series, index);
        return bodyAbs / (atrV + EPS);
    }

    public double calculateBodyCenterPosition(SymbolCandle c) {
        double center = (c.getOpen() + c.getClose()) / 2.0;
        double range  = c.getHigh() - c.getLow();
        if (range < EPS) return 0.0;
        return (center - c.getLow()) / (range + EPS);
    }

    public double calculateBodyPressureRaw(SymbolCandle c, BarSeries series, int index) {
        return calculateBodyRatio(c, series, index);
    }

    public double calculateBodyStrength(SymbolCandle c, BarSeries series, int index) {
        return Math.abs(calculateBodyPressureRaw(c, series, index));
    }

    public double calculateBodyStrengthScore(SymbolCandle c, BarSeries series, int index) {
        return Math.abs(calculateBodyPressureRaw(c, series, index));
    }

    public double calculateBodyPct(SymbolCandle c, BarSeries series, int index) {
        return bodyCacheService
                .getBodyAbsPct(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    public double calculateBodyPerc(SymbolCandle c, BarSeries series, int index) {
        return bodyCacheService
                .getBodyPerc(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    public double calculateBodyRatioAlt(SymbolCandle c, BarSeries series, int index) {
        return calculateBodyPressureRaw(c, series, index);
    }

    public double calculateBodyReturn(SymbolCandle c, BarSeries series, int index) {
        return bodyCacheService
                .getBodyReturn(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 5) V3 EXTENSIONS
    // =========================================================================

    public double calculateBodyShockAtrn(ATRIndicator atr, SymbolCandle c, BarSeries series, int index) {
        double atrV = atr.getValue(index).doubleValue();
        if (atrV < EPS) return 0.0;

        double bodyAbs = calculateBodyAbs(c, series, index);
        return bodyAbs / (atrV + EPS);
    }

    public double calculateBodySignPersistence(SymbolCandle c, BarSeries series, int index, int window) {
        return bodyCacheService
                .getBodySignPersistence(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    public double calculateBodyRunLen(SymbolCandle c, BarSeries series, int index, int maxLookback) {
        return bodyCacheService
                .getBodyRunLen(c.getSymbol(), c.getInterval(), series, maxLookback)
                .getValue(index).doubleValue();
    }
}
