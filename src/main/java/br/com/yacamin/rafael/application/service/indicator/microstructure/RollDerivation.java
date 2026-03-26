package br.com.yacamin.rafael.application.service.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.indicator.RollCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.*;
import br.com.yacamin.rafael.domain.SymbolCandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

/**
 * RollDerivation (V3+) — agora 100% baseado em TA4J Indicators (sem RafaelBar set/get).
 *
 * Indicadores usados (via RollCacheService):
 * - RollCovIndicator (ΔP)
 * - RollCovPctIndicator (ΔP%)
 * - RollSpreadIndicator
 * - RollSpreadPctIndicator
 * - ZscoreIndicator(spread)
 * - ZscoreIndicator(spreadPct)
 */
@Service
@RequiredArgsConstructor
public class RollDerivation {

    private static final double EPS = 1e-12;

    private final RollCacheService rollCacheService;

    private final ZscoreDerivation zscoreDerivation;
    private final DeltaDerivation deltaDerivation;
    private final SlopeDerivation slopeDerivation;
    private final SmoothDerivation smoothDerivation;
    private final StdHelperDerivation stdHelperDerivation;
    private final PercentileDerivation percentileDerivation;


    // =============================================================================================
    // 1) ROLL COV RAW (ΔP)
    // =============================================================================================

    public double calculateCov(SymbolCandle candle, BarSeries series, int window, int index) {
        return rollCacheService
                .getRollCov(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =============================================================================================
    // 2) ROLL SPREAD RAW  (Spread = 2 * sqrt(-cov))
    //    (mantém assinatura antiga p/ compatibilidade; "cov" é ignorado)
    // ============================================================================================

    public double calculateSpread(SymbolCandle candle, BarSeries series, int window, int index) {
        return rollCacheService
                .getRollSpread(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =============================================================================================
    // 3) ROLL COV PERCENTUAL (ΔP%)
    // =============================================================================================

    public double calculateRollCovPct(SymbolCandle candle, BarSeries series, int window, int index) {
        return rollCacheService
                .getRollCovPct(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =============================================================================================
    // 4) ROLL SPREAD PERCENTUAL (ΔP%)  (SpreadPct = 2 * sqrt(-covPct))
    // =============================================================================================

    public double calculateRollSpreadPct(SymbolCandle candle, BarSeries series, int window, int index) {
        return rollCacheService
                .getRollSpreadPct(candle.getSymbol(), candle.getInterval(), series, window)
                .getValue(index).doubleValue();
    }

    // =============================================================================================
    // 5) ROLL COV Z-SCORE (calculado em memória, lendo o indicator)
    // =============================================================================================

    public double calculateRollCovZscore(SymbolCandle candle, BarSeries series, int window, int index, int zWindow) {
        var cov = rollCacheService.getRollCov(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[zWindow];
        for (int i = 0; i < zWindow; i++) {
            vals[zWindow - 1 - i] = cov.getValue(index - i).doubleValue(); // oldest -> newest
        }

        return zscoreDerivation.zscore(vals);
    }

    // =============================================================================================
    // 6) ROLL COV PCT Z-SCORE (calculado em memória, lendo o indicator)
    // =============================================================================================

    public double calculateRollCovPctZscore(SymbolCandle candle, BarSeries series, int window, int index, int zWindow) {
        var covPct = rollCacheService.getRollCovPct(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[zWindow];
        for (int i = 0; i < zWindow; i++) {
            vals[zWindow - 1 - i] = covPct.getValue(index - i).doubleValue(); // oldest -> newest
        }

        return zscoreDerivation.zscore(vals);
    }

    // =============================================================================================
    // 7) SPREAD ACCELERATION (Δ spread)
    // =============================================================================================

    public double calculateSpreadAcceleration(SymbolCandle candle, BarSeries series, int window, int index) {
        var spread = rollCacheService.getRollSpread(candle.getSymbol(), candle.getInterval(), series, window);

        double now  = spread.getValue(index).doubleValue();
        double prev = spread.getValue(index - 1).doubleValue();

        return deltaDerivation.delta(now, prev);
    }

    // =============================================================================================
    // 8) ROLL SPREAD SLOPE
    // =============================================================================================

    public double calculateRollSpreadSlope(SymbolCandle candle, BarSeries series, int window, int index, int sWindow) {
        var spread = rollCacheService.getRollSpread(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[sWindow];
        for (int i = 0; i < sWindow; i++) {
            vals[sWindow - 1 - i] = spread.getValue(index - i).doubleValue(); // oldest -> newest
        }

        return slopeDerivation.slope(vals);
    }

    // =============================================================================================
    // 9) ROLL MOVING AVERAGE (SMOOTHING) em cima do SPREAD
    // =============================================================================================

    public double calculateRollSmooth(SymbolCandle candle, BarSeries series, int window, int index, int maWindow) {
        var spread = rollCacheService.getRollSpread(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[maWindow];
        for (int i = 0; i < maWindow; i++) {
            vals[maWindow - 1 - i] = spread.getValue(index - i).doubleValue(); // oldest -> newest
        }

        return smoothDerivation.smooth(vals);
    }

    // =============================================================================================
    // 10) ROLL SPREAD PERSISTENCE (fração de barras com spread > 0)
    // =============================================================================================

    public double calculateRollSpreadPersistence(SymbolCandle candle, BarSeries series, int window, int index, int pWindow) {
        var spread = rollCacheService.getRollSpread(candle.getSymbol(), candle.getInterval(), series, window);

        int positives = 0;
        for (int i = 0; i < pWindow; i++) {
            if (spread.getValue(index - i).doubleValue() > 0.0) positives++;
        }

        return positives / (double) pWindow;
    }

    // =============================================================================================
    // 11) ROLL SPREAD ZSCORE (Indicator)
    // =============================================================================================

    public double calculateRollSpreadZscore(SymbolCandle candle, BarSeries series, int window, int index, int zWindow) {
        return rollCacheService
                .getRollSpreadZscore(candle.getSymbol(), candle.getInterval(), series, window, zWindow)
                .getValue(index).doubleValue();
    }

    // =============================================================================================
    // 12) ROLL SPREAD PCT ZSCORE (Indicator)
    // =============================================================================================

    public double calculateRollSpreadPctZscore(SymbolCandle candle, BarSeries series, int window, int index, int zWindow) {
        return rollCacheService
                .getRollSpreadPctZscore(candle.getSymbol(), candle.getInterval(), series, window, zWindow)
                .getValue(index).doubleValue();
    }

    // =============================================================================================
    // 13) ROLL SPREAD DIVERGENCE (z(spread) - z(spreadPct))
    // =============================================================================================

    public double rollSpreadDivergence(SymbolCandle candle, BarSeries series, int window, int index, int zWindow) {
        double zRaw = calculateRollSpreadZscore(candle, series, window, index, zWindow);
        double zPct = calculateRollSpreadPctZscore(candle, series, window, index, zWindow);

        return zRaw - zPct;
    }

    // =============================================================================================
    // 14) ROLL SPREAD VOLATILITY (STD do spread numa janela)
    // =============================================================================================

    public double calculateRollSpreadVolatility(SymbolCandle candle, BarSeries series, int window, int index, int volWindow) {
        var spread = rollCacheService.getRollSpread(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[volWindow];
        for (int i = 0; i < volWindow; i++) {
            vals[volWindow - 1 - i] = spread.getValue(index - i).doubleValue(); // oldest -> newest
        }

        return stdHelperDerivation.std(vals);
    }

    // =============================================================================================
    // 15) ROLL SPREAD PERCENTILE (percentile rank do spread dentro da janela)
    // =============================================================================================

    public double calculateRollSpreadPercentile(SymbolCandle candle, BarSeries series, int window, int index, int pWindow) {
        var spread = rollCacheService.getRollSpread(candle.getSymbol(), candle.getInterval(), series, window);

        double[] vals = new double[pWindow];
        for (int i = 0; i < pWindow; i++) {
            vals[pWindow - 1 - i] = spread.getValue(index - i).doubleValue(); // oldest -> newest
        }

        double current = vals[pWindow - 1];
        return percentileDerivation.percentileRank(vals, current);
    }
}
