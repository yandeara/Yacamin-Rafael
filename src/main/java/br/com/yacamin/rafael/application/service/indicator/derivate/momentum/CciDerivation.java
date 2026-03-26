package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.PercentileDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.CCIIndicator;

@Service
@RequiredArgsConstructor
public class CciDerivation {

    private static final double EPS = 1e-12;

    private final ZscoreDerivation zscoreDerivation;
    private final PercentileDerivation percentileDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    // =============================================================================================
    // RAW
    // =============================================================================================
    public double calculateRaw(CCIIndicator cci, int last) {
        return cci.getValue(last).doubleValue();
    }

    // =============================================================================================
    // DELTA = CCI_t - CCI_{t-1}
    // =============================================================================================
    public double calculateDelta(CCIIndicator cci, int last) {
        double curr = cci.getValue(last).doubleValue();
        double prev = cci.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // DIST MID = |CCI|
    // =============================================================================================
    public double calculateDistMid(CCIIndicator cci, int last) {
        return Math.abs(cci.getValue(last).doubleValue());
    }

    // =============================================================================================
    // ZSCORE / PERCENTILE
    // =============================================================================================
    public double calculateZscore(CCIIndicator cci, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = cci.getValue(last - i).doubleValue();
        }
        return zscoreDerivation.zscore(vals);
    }

    public double calculatePercentile(CCIIndicator cci, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = cci.getValue(last - i).doubleValue();
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    // =============================================================================================
    // SHOCK (|ΔCCI|) + SHOCK_STDN(window)
    // =============================================================================================
    public double calculateShock1(CCIIndicator cci, int last) {
        return Math.abs(calculateDelta(cci, last));
    }

    public double calculateShock1Stdn(CCIIndicator cci, int last, int window) {
        if (window <= 2 || last < window) return 0.0;

        int n = window - 1;
        double[] deltas = new double[n];

        for (int i = 0; i < n; i++) {
            double a = cci.getValue(last - i).doubleValue();
            double b = cci.getValue(last - i - 1).doubleValue();
            deltas[n - 1 - i] = (a - b);
        }

        double sd = stdHelperDerivation.std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    // =============================================================================================
    // REGIME (principal: CCI_20) — Double
    // 0=OVERSOLD, 1=NEUTRAL, 2=OVERBOUGHT, 3=EXTREME
    // thresholds: -200/-100/+100/+200
    // =============================================================================================
    public double calculateRegimeState(CCIIndicator cci, int last) {
        double x = cci.getValue(last).doubleValue();

        if (x <= -200.0 || x >= 200.0) return 3.0; // EXTREME
        if (x <= -100.0) return 0.0;               // OVERSOLD
        if (x >= 100.0)  return 2.0;               // OVERBOUGHT
        return 1.0;                                 // NEUTRAL
    }

    /** % últimos window candles no mesmo lado do 0 (do lado do candle atual). */
    public double calculateRegimePersistenceSide(CCIIndicator cci, int last, int window, double mid) {
        if (window <= 0) return 0.0;

        int w = Math.min(window, last);
        if (w <= 0) return 0.0;

        double curr = cci.getValue(last).doubleValue();
        if (Math.abs(curr) < EPS) return 0.0;

        int sideNow = (curr >= mid) ? 1 : -1;

        int same = 0;
        for (int i = 0; i < w; i++) {
            double v = cci.getValue(last - i).doubleValue();
            if (Math.abs(v) < EPS) continue;
            int side = (v >= mid) ? 1 : -1;
            if (side == sideNow) same++;
        }

        return same / (double) w;
    }
}
