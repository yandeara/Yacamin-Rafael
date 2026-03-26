package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.microstructure.HasbrouckCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class HasbrouckDerivation {

    private static final double EPS = 1e-12;

    private final HasbrouckCacheService hasbCacheService;

    // =========================================================================
    // 1) HASBROUCK RAW (lambda)
    // =========================================================================
    public double calculateHasbLambda(SymbolCandle candle, BarSeries series, int window, int index) {
        return hasbCacheService
                .getHasbLambda(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 2) HASBROUCK Z-SCORE (zW=40)
    // =========================================================================
    public double calculateHasbZscore40(SymbolCandle candle, BarSeries series, int window, int index) {
        return hasbCacheService
                .getHasbZscore(candle.getSymbol(), candle.getInterval(), series, window, 40)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 3) HASBROUCK MOVING AVERAGE
    // =========================================================================
    public double calculateHasbMA(SymbolCandle candle, BarSeries series, int window, int index, int maWindow) {
        return hasbCacheService
                .getHasbMa(candle.getSymbol(), candle.getInterval(), series, window, maWindow)
                .getValue(index).doubleValue();
    }

    public double calculateHasbMA20(SymbolCandle candle, BarSeries series, int window, int index) {
        return calculateHasbMA(candle, series, window, index, 20);
    }

    // =========================================================================
    // 4) HASBROUCK DIVERGENCE (RAW − MA20)
    // =========================================================================
    public double calculateHasbDivergence(SymbolCandle candle, BarSeries series, int window, int index) {
        double raw = calculateHasbLambda(candle, series, window, index);
        double ma  = calculateHasbMA20(candle, series, window, index);
        return raw - ma;
    }

    // =========================================================================
    // 5) HASBROUCK SLOPE (slp_w20)
    // =========================================================================
    public double calculateHasbSlopeW20(SymbolCandle candle, BarSeries series, int window, int index) {
        return hasbCacheService
                .getHasbSlope(candle.getSymbol(), candle.getInterval(), series, window, 20)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 6) HASBROUCK VOLATILITY (STD40)
    // =========================================================================
    public double calculateHasbVolatility40(SymbolCandle candle, BarSeries series, int window, int index) {
        return hasbCacheService
                .getHasbVol(candle.getSymbol(), candle.getInterval(), series, window, 40)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 7) HASBROUCK STABILITY (Vol40 / MA40)
    // =========================================================================
    public double calculateHasbStability40(SymbolCandle candle, BarSeries series, int window, int index) {
        double vol40 = calculateHasbVolatility40(candle, series, window, index);
        double ma40  = calculateHasbMA(candle, series, window, index, 40);

        if (Math.abs(ma40) < EPS) return 0.0;
        return vol40 / (ma40 + EPS);
    }

    // =========================================================================
    // 8) HASBROUCK PERCENTILE (w40)
    // =========================================================================
    public double calculateHasbPercentileW40(SymbolCandle candle, BarSeries series, int window, int index) {
        return hasbCacheService
                .getHasbPercentile(candle.getSymbol(), candle.getInterval(), series, window, 40)
                .getValue(index).doubleValue();
    }

    // =========================================================================
    // 9) HASBROUCK / KYLE RATIO
    // =========================================================================
    public double calculateHasbToKyleRatio(double hasbLambda, double kyleLambda) {
        return Math.abs(hasbLambda) / (Math.abs(kyleLambda) + EPS);
    }
}
