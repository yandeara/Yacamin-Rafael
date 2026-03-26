package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.PercentileDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.CMOIndicator;

@Service
@RequiredArgsConstructor
public class CmoDerivation {

    private static final double EPS = 1e-12;

    private final ZscoreDerivation zscoreDerivation;
    private final PercentileDerivation percentileDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    // =============================================================================================
    // RAW
    // =============================================================================================
    public double calculateRaw(CMOIndicator cmo, int last) {
        return cmo.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Delta = CMO_t - CMO_{t-1}
    // =============================================================================================
    public double calculateDelta(CMOIndicator cmo, int last) {
        double curr = cmo.getValue(last).doubleValue();
        double prev = cmo.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // DISTMID = |CMO - 0| = |CMO|
    // =============================================================================================
    public double calculateDistMid(CMOIndicator cmo, int last) {
        double x = cmo.getValue(last).doubleValue();
        return Math.abs(x);
    }

    // =============================================================================================
    // V3 — ZSCORE / PERCENTILE / SHOCK
    // =============================================================================================

    /** zscore do CMO em janela (oldest -> newest) */
    public double calculateZscore(CMOIndicator cmo, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = cmo.getValue(last - i).doubleValue();
        }
        return zscoreDerivation.zscore(vals);
    }

    /** percentile rank do CMO atual em relação à janela */
    public double calculatePercentile(CMOIndicator cmo, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = cmo.getValue(last - i).doubleValue();
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    /** shock bruto: |ΔCMO_1| */
    public double calculateShock1(CMOIndicator cmo, int last) {
        return Math.abs(calculateDelta(cmo, last));
    }

    /** shock normalizado pela vol(ΔCMO) em janela */
    public double calculateShock1Stdn(CMOIndicator cmo, int last, int window) {
        if (window <= 2 || last < window) return 0.0;

        int n = window - 1;
        double[] deltas = new double[n];

        for (int i = 0; i < n; i++) {
            double a = cmo.getValue(last - i).doubleValue();
            double b = cmo.getValue(last - i - 1).doubleValue();
            deltas[n - 1 - i] = (a - b);
        }

        double sd = stdHelperDerivation.std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    // =============================================================================================
    // V3 — REGIME STATE / PERSISTENCE (Double)
    // mid = 0.0
    // 0=OVERSOLD, 1=NEUTRAL, 2=OVERBOUGHT, 3=EXTREME
    // =============================================================================================

    public double calculateRegimeState(CMOIndicator cmo, int last) {
        double x = cmo.getValue(last).doubleValue();

        // EXTREME
        if (x <= -80.0 || x >= 80.0) return 3.0;

        // OVERSOLD (negativo forte)
        if (x <= -50.0) return 0.0;

        // OVERBOUGHT (positivo forte)
        if (x >= 50.0) return 2.0;

        // NEUTRAL
        return 1.0;
    }

    /** % últimos window candles no mesmo lado do 0 (do lado do candle atual). */
    public double calculateRegimePersistenceSide(CMOIndicator cmo, int last, int window, double mid) {
        if (window <= 0) return 0.0;

        int w = Math.min(window, last);
        if (w <= 0) return 0.0;

        double curr = cmo.getValue(last).doubleValue();
        if (Math.abs(curr) < EPS) return 0.0; // neutro => sem persistência direcional

        int sideNow = (curr >= mid) ? 1 : -1;

        int same = 0;
        for (int i = 0; i < w; i++) {
            double v = cmo.getValue(last - i).doubleValue();
            if (Math.abs(v) < EPS) continue; // ignora zeros para não distorcer
            int side = (v >= mid) ? 1 : -1;
            if (side == sideNow) same++;
        }

        return same / (double) w;
    }
}
