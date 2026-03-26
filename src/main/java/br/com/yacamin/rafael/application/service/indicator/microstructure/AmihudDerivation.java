package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.AmihudCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.*;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class AmihudDerivation {

    private static final double EPS = 1e-9;

    private final CloseCacheService closeCacheService;
    private final ZscoreDerivation zscoreDerivation;
    private final RelativeDerivation relativeDerivation;
    private final SlopeDerivation slopeDerivation;
    private final DeltaDerivation deltaDerivation;
    private final SmoothDerivation smoothDerivation;
    private final StdHelperDerivation stdHelperDerivation;
    private final PercentileDerivation percentileDerivation;

    private final AmihudCacheService amihudCacheService;

    // =========================================================================
    // 1. AMIHUD RAW (ILLIQ)
    // =========================================================================
    public double calculateAmihud(SymbolCandle candle, BarSeries series, int index) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);
        return amihud.getValue(index).doubleValue();
    }

    // =========================================================================
    // 2. AMIHUD Z-SCORE (20 / 80)
    // =========================================================================
    public double calculateAmihudZscore(SymbolCandle candle, BarSeries series, int index, int zWindow) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);

        double[] vals = new double[zWindow];
        for (int i = 0; i < zWindow; i++) {
            vals[zWindow - 1 - i] = amihud.getValue(index - i).doubleValue(); // oldest -> newest
        }
        return zscoreDerivation.zscore(vals);
    }


    public double calculateAmihudRelative(SymbolCandle candle, BarSeries series, int index, int window) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = amihud.getValue(index - i).doubleValue();
        }
        return relativeDerivation.relative(vals); // last / mean
    }

    // =========================================================================
    // 4. AMIHUD SLOPE
    // =========================================================================
    public double calculateAmihudSlope(SymbolCandle candle, BarSeries series, int index, int window) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = amihud.getValue(index - i).doubleValue();
        }
        return slopeDerivation.slope(vals);
    }


    public double calculateAmihudAcceleration(SymbolCandle candle, BarSeries series, int index, int w) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);

        double now  = amihud.getValue(index).doubleValue();
        double past = amihud.getValue(index - w).doubleValue();
        return deltaDerivation.delta(now, past);
    }

    public double calculateAmihudSmooth(SymbolCandle candle, BarSeries series, int index, int window) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = amihud.getValue(index - i).doubleValue();
        }
        return smoothDerivation.smooth(vals);
    }

    public double calculateAmihudVolatility(SymbolCandle candle, BarSeries series, int index, int window) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = amihud.getValue(index - i).doubleValue();
        }
        return stdHelperDerivation.std(vals);
    }

    public double calculateAmihudVolatilityRelative(double amihudVolShort, double amihudVolLong) {
        if (amihudVolLong < EPS) return 0.0;
        return amihudVolShort / amihudVolLong;
    }

    public double calculateAmihudPersistence(SymbolCandle candle, BarSeries series, int index, int window) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);

        int positives = 0;
        for (int i = 0; i < window; i++) {
            if (amihud.getValue(index - i).doubleValue() > 0) positives++;
        }
        return positives / (double) window;
    }


    // =========================================================================
    // 10. DIVERGENCE
    // =========================================================================
    public double calculateAmihudDivergence(SymbolCandle candle, BarSeries series, int index, int window) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = amihud.getValue(index - i).doubleValue();
        }

        double current = vals[window - 1];

        double sum = 0.0;
        for (double v : vals) sum += v;
        double mean = sum / window;

        return current - mean;
    }

    // =========================================================================
    // 10. Turnover
    // =========================================================================
    public double calculateAmihudTurnover(SymbolCandle candle, BarSeries series, int index) {
        double volume = series.getBar(index).getVolume().doubleValue();

        ClosePriceIndicator close =
                closeCacheService.getClosePrice(candle.getSymbol(), candle.getInterval(), series);

        double c1 = close.getValue(index).doubleValue();
        double c0 = close.getValue(index - 1).doubleValue();

        double retPct = (c1 - c0) / c0;
        double absRet = Math.abs(retPct);

        if (absRet < EPS) return 0.0;
        if (volume < EPS) return 0.0;

        return volume / absRet;
    }


    // =========================================================================
    // 10. Percentile
    // =========================================================================
    public double calculateAmihudPercentile(SymbolCandle candle, BarSeries series, int index, int pWindow) {
        var amihud = amihudCacheService.getAmihudRaw(candle.getSymbol(), candle.getInterval(), series);

        double[] vals = new double[pWindow];
        for (int i = 0; i < pWindow; i++) {
            vals[pWindow - 1 - i] = amihud.getValue(index - i).doubleValue();
        }

        double current = vals[pWindow - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    public double calculateAmihudSigned(SymbolCandle candle, BarSeries series, int index) {
        ClosePriceIndicator close =
                closeCacheService.getClosePrice(candle.getSymbol(), candle.getInterval(), series);

        double volume = series.getBar(index).getVolume().doubleValue();

        double c1 = close.getValue(index).doubleValue();
        double c0 = close.getValue(index - 1).doubleValue();

        double ret = (c1 - c0) / c0;
        if (Math.abs(volume) < EPS) return 0.0;

        return ret / volume;
    }

    public double calculateAmihudLRMR(SymbolCandle candle, BarSeries series, int index) {
        double ma10 = calculateAmihudSmooth(candle, series, index, 10);
        double ma40 = calculateAmihudSmooth(candle, series, index, 40);
        return ma10 - ma40;
    }

    public double calculateAmihudStability40(SymbolCandle candle, BarSeries series, int index) {
        double vol40 = calculateAmihudVolatility(candle, series, index, 40);
        double ma40  = calculateAmihudSmooth(candle, series, index, 40);

        if (Math.abs(ma40) < EPS) return 0.0;
        return vol40 / ma40;
    }

    // ==========================================================================
    // AMIHUD REGIME STATE
    // ==========================================================================
    public double calculateAmihudRegimeState(
            double amihudZscore80,
            double amihudAtrN,
            double amihudStability40
    ) {

        if (amihudZscore80 < 0.5 && amihudAtrN < 1.0) {
            return 0; // LOW_IMPACT
        }

        if (amihudZscore80 < 1.5 && amihudAtrN < 2.0) {
            return 1; // NORMAL
        }

        if (amihudZscore80 < 2.5 || amihudStability40 > 1.2) {
            return 2; // HIGH
        }

        return 3; // CRITICAL
    }

    // ==========================================================================
    // AMIHUD TREND ALIGNMENT
    // ==========================================================================
    public double calculateAmihudTrendAlignment(
            SymbolCandle candle,
            BarSeries series,
            int index,
            int slopeWindow
    ) {

        ClosePriceIndicator close =
                closeCacheService.getClosePrice(candle.getSymbol(), candle.getInterval(), series);

        double c1 = close.getValue(index).doubleValue();
        double c0 = close.getValue(index - slopeWindow).doubleValue();

        if (Math.abs(c0) < EPS) return 0.0;

        double priceRet = (c1 - c0) / c0;
        double priceSign = Math.signum(priceRet);

        double amihudSlope = calculateAmihudSlope(candle, series, index, slopeWindow);

        return priceSign * amihudSlope;
    }

    // ==========================================================================
    // AMIHUD BREAKDOWN RISK
    // ==========================================================================
    public double calculateAmihudBreakdownRisk(
            double amihudZscore80,
            double amihudStability40
    ) {

        if (amihudStability40 < EPS) return 0.0;

        double raw = amihudZscore80 * (1.0 / amihudStability40);

        // sigmoid
        return 1.0 / (1.0 + Math.exp(-raw));
    }

    // ==========================================================================
    // AMIHUD FRACTAL RATIO (5m / 30m)
    // ==========================================================================
    public double calculateAmihudFractalRatio(
            double amihud5m,
            double amihud30m
    ) {

        if (Math.abs(amihud30m) < EPS) return 0.0;

        return amihud5m / amihud30m;
    }

    // ==========================================================================
    // AMIHUD REGIME CONFIDENCE
    // ==========================================================================
    public double calculateAmihudRegimeConfidence(
            SymbolCandle candle,
            BarSeries series,
            int index,
            int window
    ) {

        double std = calculateAmihudVolatility(candle, series, index, window);
        double mean = calculateAmihudSmooth(candle, series, index, window);

        if (Math.abs(mean) < EPS) return 0.0;

        double cv = std / mean; // coeficiente de variação

        // normaliza para [0,1]
        return Math.max(0.0, 1.0 - cv);
    }





}
