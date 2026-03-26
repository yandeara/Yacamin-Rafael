package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.PercentileDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.WilliamsRIndicator;

@Service
@RequiredArgsConstructor
public class WprDerivation {

    private static final double EPS = 1e-12;

    private final ZscoreDerivation zscoreDerivation;
    private final PercentileDerivation percentileDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    // =============================================================================================
    // RAW
    // =============================================================================================
    public double calculateRaw(WilliamsRIndicator wpr, int last) {
        return wpr.getValue(last).doubleValue();
    }

    // =============================================================================================
    // DELTA = WPR_t - WPR_{t-1}
    // =============================================================================================
    public double calculateDelta(WilliamsRIndicator wpr, int last) {
        double curr = wpr.getValue(last).doubleValue();
        double prev = wpr.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // DISTMID = |WPR - mid|, mid default = -50
    // =============================================================================================
    public double calculateDistMid(WilliamsRIndicator wpr, int last, double mid) {
        double x = wpr.getValue(last).doubleValue();
        return Math.abs(x - mid);
    }

    // =============================================================================================
    // V3 — ZSCORE / PERCENTILE / SHOCK
    // =============================================================================================

    public double calculateZscore(WilliamsRIndicator wpr, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = wpr.getValue(last - i).doubleValue();
        }
        return zscoreDerivation.zscore(vals);
    }

    public double calculatePercentile(WilliamsRIndicator wpr, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = wpr.getValue(last - i).doubleValue();
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    public double calculateShock1(WilliamsRIndicator wpr, int last) {
        return Math.abs(calculateDelta(wpr, last));
    }

    public double calculateShock1Stdn(WilliamsRIndicator wpr, int last, int window) {
        if (window <= 2 || last < window) return 0.0;

        int n = window - 1;
        double[] deltas = new double[n];

        for (int i = 0; i < n; i++) {
            double a = wpr.getValue(last - i).doubleValue();
            double b = wpr.getValue(last - i - 1).doubleValue();
            deltas[n - 1 - i] = (a - b);
        }

        double sd = stdHelperDerivation.std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    // =============================================================================================
    // V3 — REGIME STATE / PERSISTENCE (Double)
    // mid = -50
    // 0=OVERSOLD, 1=NEUTRAL, 2=OVERBOUGHT, 3=EXTREME
    // =============================================================================================

    public double calculateRegimeState(WilliamsRIndicator wpr, int last) {
        double x = wpr.getValue(last).doubleValue(); // [-100..0]

        // EXTREME
        if (x <= -95.0 || x >= -5.0) return 3.0;

        // OVERSOLD (muito negativo)
        if (x <= -80.0) return 0.0;

        // OVERBOUGHT (próximo de 0)
        if (x >= -20.0) return 2.0;

        // NEUTRAL
        return 1.0;
    }

    /** % últimos window candles no mesmo lado do mid (default -50) do candle atual */
    public double calculateRegimePersistenceSide(WilliamsRIndicator wpr, int last, int window, double mid) {
        if (window <= 0) return 0.0;

        int w = Math.min(window, last);
        if (w <= 0) return 0.0;

        double curr = wpr.getValue(last).doubleValue();
        int sideNow = (curr >= mid) ? 1 : -1;

        int same = 0;
        for (int i = 0; i < w; i++) {
            double v = wpr.getValue(last - i).doubleValue();
            int side = (v >= mid) ? 1 : -1;
            if (side == sideNow) same++;
        }

        return same / (double) w;
    }
}
