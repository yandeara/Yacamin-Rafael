package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.PercentileDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.RSIIndicator;

@Service
@RequiredArgsConstructor
public class RsiDerivation {

    private static final double EPS = 1e-12;

    private final ZscoreDerivation zscoreDerivation;
    private final PercentileDerivation percentileDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    public double calculateDelta(RSIIndicator rsi, int last) {
        double curr = rsi.getValue(last).doubleValue();
        double prev = rsi.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // ROC: (RSI_t - RSI_{t-period}) / RSI_{t-period}
    // =============================================================================================
    public double calculateRoc(RSIIndicator rsi, int last, int period) {
        double prev = rsi.getValue(last - period).doubleValue();
        double curr = rsi.getValue(last).doubleValue();
        if (Math.abs(prev) < EPS) return 0.0; // Regra de Ouro (evita explode)
        return (curr - prev) / prev;
    }

    // =============================================================================================
    // Velocity = (RSI_t - RSI_{t-d}) / d
    // =============================================================================================
    public double calculateVelocity(RSIIndicator rsi, int last, int dist) {
        double curr = rsi.getValue(last).doubleValue();
        double prev = rsi.getValue(last - dist).doubleValue();
        if (dist <= 0) return 0.0;
        return (curr - prev) / dist;
    }

    // =============================================================================================
    // Acceleration — second difference of RSI
    // =============================================================================================
    public double calculateAcceleration(RSIIndicator rsi, int last, int dist) {
        if (dist <= 0) return 0.0;

        double curr  = rsi.getValue(last).doubleValue();
        double prev  = rsi.getValue(last - dist).doubleValue();
        double prev2 = rsi.getValue(last - 2 * dist).doubleValue();

        double v1 = curr - prev;
        double v2 = prev - prev2;

        return v1 - v2;
    }

    // =============================================================================================
    // DISTMID = |RSI - 50|
    // =============================================================================================
    public double calculateDistMid(RSIIndicator rsi, int last) {
        double x = rsi.getValue(last).doubleValue();
        return Math.abs(x - 50.0);
    }

    // =============================================================================================
    // TAIL_UP = max(RSI - upper, 0)
    // =============================================================================================
    public double calculateTailUp(RSIIndicator rsi, int last, double upper) {
        double x = rsi.getValue(last).doubleValue();
        double diff = x - upper;
        return diff > 0 ? diff : 0.0;
    }

    // =============================================================================================
    // TAIL_DW = max(lower - RSI, 0)
    // =============================================================================================
    public double calculateTailDown(RSIIndicator rsi, int last, double lower) {
        double x = rsi.getValue(last).doubleValue();
        double diff = lower - x;
        return diff > 0 ? diff : 0.0;
    }

    // =============================================================================================
    // V3 — ZSCORE / PERCENTILE / SHOCK
    // =============================================================================================

    /** zscore do RSI em janela (oldest -> newest) */
    public double calculateZscore(RSIIndicator rsi, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = rsi.getValue(last - i).doubleValue();
        }
        return zscoreDerivation.zscore(vals);
    }

    /** percentile rank do RSI atual em relação à janela */
    public double calculatePercentile(RSIIndicator rsi, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = rsi.getValue(last - i).doubleValue();
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    /** shock bruto: |ΔRSI_1| */
    public double calculateShock1(RSIIndicator rsi, int last) {
        double curr = rsi.getValue(last).doubleValue();
        double prev = rsi.getValue(last - 1).doubleValue();
        return Math.abs(curr - prev);
    }

    /** shock normalizado pela vol(ΔRSI) em janela */
    public double calculateShock1Stdn(RSIIndicator rsi, int last, int window) {
        if (window <= 2 || last < window) return 0.0;

        // ΔRSI para a janela (window-1 deltas)
        int n = window - 1;
        double[] deltas = new double[n];
        for (int i = 0; i < n; i++) {
            double a = rsi.getValue(last - i).doubleValue();
            double b = rsi.getValue(last - i - 1).doubleValue();
            deltas[n - 1 - i] = (a - b);
        }

        double sd = stdHelperDerivation.std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    // =============================================================================================
    // V3 — REGIME STATE / REGIME PERSISTENCE (Double)
    // =============================================================================================

    /**
     * mom_rsi_14_regime_state (Double):
     * 0=OVERSOLD, 1=NEUTRAL, 2=OVERBOUGHT, 3=EXTREME
     */
    public double calculateRegimeState(RSIIndicator rsi, int last) {
        double v = rsi.getValue(last).doubleValue();

        // EXTREME
        if (v <= 20.0 || v >= 80.0) return 3.0;

        // OVERSOLD
        if (v <= 30.0) return 0.0;

        // OVERBOUGHT
        if (v >= 70.0) return 2.0;

        // NEUTRAL
        return 1.0;
    }

    /**
     * mom_rsi_14_regime_prst_w20:
     * % dos últimos N candles em que RSI ficou do mesmo lado do 50 que o candle atual.
     */
    public double calculateRegimePersistenceSide(RSIIndicator rsi, int last, int window, double mid) {
        if (window <= 0) return 0.0;

        int w = Math.min(window, last); // evita index negativo
        if (w <= 0) return 0.0;

        double curr = rsi.getValue(last).doubleValue();
        int sideNow = (curr >= mid) ? 1 : -1;

        int same = 0;
        for (int i = 0; i < w; i++) {
            double x = rsi.getValue(last - i).doubleValue();
            int side = (x >= mid) ? 1 : -1;
            if (side == sideNow) same++;
        }

        return same / (double) w;
    }

}
