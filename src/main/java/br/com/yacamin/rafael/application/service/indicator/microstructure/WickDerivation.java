package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.microstructure.WickCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Service
@RequiredArgsConstructor
public class WickDerivation {

    private static final double EPS = 1e-12;

    private final WickCacheService wickCacheService;

    // =========================================================================
    // 1) WICK RAW
    // =========================================================================

    public double calculateUpperWick(SymbolCandle c, BarSeries series, int index) {
        return wickCacheService
                .getUpperWick(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    public double calculateLowerWick(SymbolCandle c, BarSeries series, int index) {
        return wickCacheService
                .getLowerWick(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    public double calculateUpperWickPct(SymbolCandle c, BarSeries series, int index) {
        var bar = series.getBar(index);
        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        if (range < EPS) return 0.0;

        double upper = calculateUpperWick(c, series, index);
        return upper / (range + EPS);
    }

    public double calculateLowerWickPct(SymbolCandle c, BarSeries series, int index) {
        var bar = series.getBar(index);
        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        if (range < EPS) return 0.0;

        double lower = calculateLowerWick(c, series, index);
        return lower / (range + EPS);
    }

    // =========================================================================
    // 2) WICK STRUCTURE
    // =========================================================================

    public double calculateWickImbalance(SymbolCandle c, BarSeries series, int index) {
        return wickCacheService
                .getWickImbalance(c.getSymbol(), c.getInterval(), series)
                .getValue(index).doubleValue();
    }

    public double calculateShadowRatio(SymbolCandle c, BarSeries series, int index) {
        var bar = series.getBar(index);

        double upper = calculateUpperWick(c, series, index);
        double lower = calculateLowerWick(c, series, index);

        double shadows = upper + lower;
        double bodyAbs = Math.abs(bar.getClosePrice().doubleValue() - bar.getOpenPrice().doubleValue());
        double denom = shadows + bodyAbs;

        if (denom < EPS) return 0.0;
        return shadows / (denom + EPS);
    }

    public double calculateWickBodyAlignment(SymbolCandle c, BarSeries series, int index) {
        var bar = series.getBar(index);
        double body = bar.getClosePrice().doubleValue() - bar.getOpenPrice().doubleValue();

        if (Math.abs(body) < EPS) return 0.0;

        double wickImb = calculateWickImbalance(c, series, index);
        return Math.signum(body) * wickImb;
    }

    // =========================================================================
    // 3) SCORES (aliases)
    // =========================================================================

    public double calculateWickPressureScore(SymbolCandle c, BarSeries series, int index) {
        return calculateWickImbalance(c, series, index);
    }

    public double calculateShadowImbalanceScore(SymbolCandle c, BarSeries series, int index) {
        return calculateWickImbalance(c, series, index);
    }

    // =========================================================================
    // 4) DOMINANCE
    // =========================================================================

    public double calculateWickDominance(SymbolCandle c, BarSeries series, int index) {
        double upper = calculateUpperWick(c, series, index);
        double lower = calculateLowerWick(c, series, index);
        double sum = upper + lower;

        if (sum < EPS) return 0.0;
        return Math.max(upper, lower) / (sum + EPS);
    }

    // =========================================================================
    // 5) EXHAUSTION
    // =========================================================================

    public double calculateWickExhaustion(SymbolCandle c, BarSeries series, int index) {
        var bar = series.getBar(index);

        double wickImbAbs = Math.abs(calculateWickImbalance(c, series, index));

        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        double bodyRatio = Math.abs((bar.getClosePrice().doubleValue() - bar.getOpenPrice().doubleValue()) / (range + EPS));

        return wickImbAbs * (1.0 - bodyRatio);
    }

    // =========================================================================
    // 6) DYNAMICS (cross-window) — agora via Indicators
    // =========================================================================

    public double calculateWickImbalanceSlope(SymbolCandle c, BarSeries series, int index, int window) {
        return wickCacheService
                .getWickImbalanceSlope(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    public double calculateWickImbalanceVol(SymbolCandle c, BarSeries series, int index, int window) {
        return wickCacheService
                .getWickImbalanceVol(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    public double calculateUpperWickMa(SymbolCandle c, BarSeries series, int index, int window) {
        return wickCacheService
                .getUpperWickMa(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    public double calculateLowerWickMa(SymbolCandle c, BarSeries series, int index, int window) {
        return wickCacheService
                .getLowerWickMa(c.getSymbol(), c.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 7) WICK-BASED RETURNS (single candle)
    // =========================================================================

    public double calculateUpperWickReturn(BarSeries series, int index) {
        if (index <= 0) return 0.0;

        var bar = series.getBar(index);
        var prev = series.getBar(index - 1);

        double prevClose = prev.getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0.0;

        return (bar.getHighPrice().doubleValue() - prevClose) / (prevClose + EPS);
    }

    public double calculateLowerWickReturn(BarSeries series, int index) {
        if (index <= 0) return 0.0;

        var bar = series.getBar(index);
        var prev = series.getBar(index - 1);

        double prevClose = prev.getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0.0;

        return (bar.getLowPrice().doubleValue() - prevClose) / (prevClose + EPS);
    }

    // =========================================================================
    // V3 EXTENSIONS
    // =========================================================================

    public double calculateTotalWick(SymbolCandle c, BarSeries series, int index) {
        double upper = calculateUpperWick(c, series, index);
        double lower = calculateLowerWick(c, series, index);
        return upper + lower;
    }

    public double calculateTotalWickPct(SymbolCandle c, BarSeries series, int index) {
        var bar = series.getBar(index);
        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        if (range < EPS) return 0.0;

        return calculateTotalWick(c, series, index) / (range + EPS);
    }

    public double calculateTotalWickAtrn(ATRIndicator atr, SymbolCandle c, BarSeries series, int index) {
        double atrV = atr.getValue(index).doubleValue();
        if (atrV < EPS) return 0.0;

        double totalWick = calculateTotalWick(c, series, index);
        return totalWick / (atrV + EPS);
    }

    /** (upper-lower)/(upper+lower+eps) */
    public double calculateWickImbalanceNorm(SymbolCandle c, BarSeries series, int index) {
        double upper = calculateUpperWick(c, series, index);
        double lower = calculateLowerWick(c, series, index);
        double denom = upper + lower;

        if (denom < EPS) return 0.0;
        return (upper - lower) / (denom + EPS);
    }

    /** slope(close_pos_norm, 20) — agora via Indicator */
    public double calculateClosePosSlopeW20(SymbolCandle c, BarSeries series, int index) {
        return wickCacheService
                .getClosePosSlope(c.getSymbol(), c.getInterval(), series, 20)
                .getValue(index).doubleValue();
    }
}
