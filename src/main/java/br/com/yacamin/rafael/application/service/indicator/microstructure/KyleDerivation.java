package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.KyleCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.*;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class KyleDerivation {

    private static final double EPS = 1e-12;

    private final KyleCacheService kyleCacheService;

    private final ZscoreDerivation zscoreDerivation;
    private final RelativeDerivation relativeDerivation;
    private final SlopeDerivation slopeDerivation;
    private final DeltaDerivation deltaDerivation;
    private final SmoothDerivation smoothDerivation;
    private final StdHelperDerivation stdHelperDerivation;
    private final PercentileDerivation percentileDerivation;

    // =========================================================================
    // 1) KYLE RAW (lambda) — window: 4/16/48/96/200/288
    // =========================================================================
    public double calculateKyleLambda(SymbolCandle candle, BarSeries series, int index, int window) {
        if (index < 0) return 0.0;

        return kyleCacheService
                .getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 1b) KYLE SIGNED (instant ret/ofi)
    // =========================================================================
    public double calculateKyleSigned(SymbolCandle candle, BarSeries series, int index) {
        if (index <= 0) return 0.0;

        return kyleCacheService
                .getKyleSigned(candle.getSymbol(), candle.getInterval(), series)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 2) Z-SCORE (sobre lambda)
    // =========================================================================
    public double calculateKyleZscore(SymbolCandle candle, BarSeries series, int index, int window, int zWindow) {
        if (zWindow <= 1 || index - (zWindow - 1) < 0) return 0.0;

        var lambda = kyleCacheService.getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[zWindow];
        for (int i = 0; i < zWindow; i++) {
            vals[zWindow - 1 - i] = lambda.getValue(index - i).doubleValue(); // oldest -> newest
        }

        return zscoreDerivation.zscore(vals);
    }

    // =========================================================================
    // 3) RELATIVE (last / mean) sobre lambda
    // =========================================================================
    public double calculateKyleRelative(SymbolCandle candle, BarSeries series, int window, int index, int relWindow) {
        if (relWindow <= 1 || index - (relWindow - 1) < 0) return 0.0;

        var lambda = kyleCacheService.getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[relWindow];
        for (int i = 0; i < relWindow; i++) {
            vals[relWindow - 1 - i] = lambda.getValue(index - i).doubleValue();
        }

        return relativeDerivation.relative(vals);
    }

    // =========================================================================
    // 4) SLOPE (linreg) sobre lambda
    // =========================================================================
    public double calculateKyleSlope(SymbolCandle candle, BarSeries series, int window, int index, int sWindow) {
        if (sWindow <= 1 || index - (sWindow - 1) < 0) return 0.0;

        var lambda = kyleCacheService.getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[sWindow];
        for (int i = 0; i < sWindow; i++) {
            vals[sWindow - 1 - i] = lambda.getValue(index - i).doubleValue();
        }

        return slopeDerivation.slope(vals);
    }

    // =========================================================================
    // 5) ACCELERATION (delta entre now e index-accWindow)
    // =========================================================================
    public double calculateKyleAcceleration(SymbolCandle candle, BarSeries series, int window, int index, int accWindow) {
        if (accWindow <= 0 || index - accWindow < 0) return 0.0;

        var lambda = kyleCacheService.getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window);

        double now  = lambda.getValue(index).doubleValue();
        double past = lambda.getValue(index - accWindow).doubleValue();

        return deltaDerivation.delta(now, past);
    }

    // =========================================================================
    // 6) MOVING AVERAGE (smooth) sobre lambda
    // =========================================================================
    public double calculateKyleMA(SymbolCandle candle, BarSeries series, int window, int index, int maWindow) {
        if (maWindow <= 1 || index - (maWindow - 1) < 0) return 0.0;

        var lambda = kyleCacheService.getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[maWindow];
        for (int i = 0; i < maWindow; i++) {
            vals[maWindow - 1 - i] = lambda.getValue(index - i).doubleValue();
        }

        return smoothDerivation.smooth(vals);
    }

    // =========================================================================
    // 7) VOLATILITY (std) sobre lambda
    // =========================================================================
    public double calculateKyleVolatility(SymbolCandle candle, BarSeries series, int window, int index, int volWindow) {
        if (volWindow <= 1 || index - (volWindow - 1) < 0) return 0.0;

        var lambda = kyleCacheService.getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[volWindow];
        for (int i = 0; i < volWindow; i++) {
            vals[volWindow - 1 - i] = lambda.getValue(index - i).doubleValue();
        }

        return stdHelperDerivation.std(vals);
    }

    // =========================================================================
    // 8) VOL RELATIVE
    // =========================================================================
    public double calculateVolRelative(double volShort, double volLong) {
        if (Math.abs(volLong) < EPS) return 0.0;
        return volShort / volLong;
    }

    // =========================================================================
    // 9) PERSISTENCE (fração de lambdas > 0)
    // =========================================================================
    public double calculateKylePersistence(SymbolCandle candle, BarSeries series, int window, int index, int pWindow) {
        if (pWindow <= 0 || index - (pWindow - 1) < 0) return 0.0;

        var lambda = kyleCacheService.getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window);

        int positives = 0;
        for (int i = 0; i < pWindow; i++) {
            if (lambda.getValue(index - i).doubleValue() > 0.0) positives++;
        }

        return (double) positives / (double) pWindow;
    }

    // =========================================================================
    // 10) DIVERGENCE (RAW − MA20)
    // =========================================================================
    public double calculateKyleDivergence(SymbolCandle candle, BarSeries series, int window, int index) {
        if (index < 0) return 0.0;

        var lambda = kyleCacheService.getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window);

        double current = lambda.getValue(index).doubleValue();
        double ma20    = calculateKyleMA(candle, series, window, index, 20);

        return current - ma20;
    }

    // =========================================================================
    // 11) PERCENTILE (percentile rank na janela)
    // =========================================================================
    public double calculateKylePercentile(SymbolCandle candle, BarSeries series, int window, int index, int pWindow) {
        if (pWindow <= 1 || index - (pWindow - 1) < 0) return 0.0;

        var lambda = kyleCacheService.getKyleLambda(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[pWindow];
        for (int i = 0; i < pWindow; i++) {
            vals[pWindow - 1 - i] = lambda.getValue(index - i).doubleValue();
        }

        double current = vals[pWindow - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    // =========================================================================
    // 12) LRMR (MA10 − MA40)
    // =========================================================================
    public double calculateKyleLRMR(SymbolCandle candle, BarSeries series, int window, int index) {
        double ma10 = calculateKyleMA(candle, series, window, index, 10);
        double ma40 = calculateKyleMA(candle, series, window, index, 40);
        return ma10 - ma40;
    }

    // =========================================================================
    // 13) STABILITY (Vol40 / MA40)
    // =========================================================================
    public double calculateKyleStability(SymbolCandle candle, BarSeries series, int window, int index) {

        double vol40 = calculateKyleVolatility(candle, series, window, index, 40);
        double ma40  = calculateKyleMA(candle, series, window, index, 40);

        if (Math.abs(ma40) < EPS) return 0.0;
        return vol40 / ma40;
    }
}
