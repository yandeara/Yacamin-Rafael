package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.PercentileDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.ROCIndicator;

@Service
@RequiredArgsConstructor
public class RocDerivation {

    private static final double EPS = 1e-12;

    private final ZscoreDerivation zscoreDerivation;
    private final PercentileDerivation percentileDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    // =============================================================================================
    // RAW ROC
    // =============================================================================================
    public double calculateRaw(ROCIndicator roc, int last) {
        return roc.getValue(last).doubleValue();
    }

    // =============================================================================================
    // ABS
    // =============================================================================================
    public double calculateAbs(ROCIndicator roc, int last) {
        return Math.abs(calculateRaw(roc, last));
    }

    // =============================================================================================
    // ZSCORE / PERCENTILE (window)
    // =============================================================================================
    public double calculateZscore(ROCIndicator roc, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = roc.getValue(last - i).doubleValue();
        }
        return zscoreDerivation.zscore(vals);
    }

    public double calculatePercentile(ROCIndicator roc, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = roc.getValue(last - i).doubleValue();
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    // =============================================================================================
    // SHOCK (|ΔROC|) + SHOCK_STDN
    // =============================================================================================
    public double calculateShock1(ROCIndicator roc, int last) {
        double curr = roc.getValue(last).doubleValue();
        double prev = roc.getValue(last - 1).doubleValue();
        return Math.abs(curr - prev);
    }

    public double calculateShock1Stdn(ROCIndicator roc, int last, int window) {
        if (window <= 2 || last < window) return 0.0;

        int n = window - 1;
        double[] deltas = new double[n];

        for (int i = 0; i < n; i++) {
            double a = roc.getValue(last - i).doubleValue();
            double b = roc.getValue(last - i - 1).doubleValue();
            deltas[n - 1 - i] = (a - b);
        }

        double sd = stdHelperDerivation.std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    // =============================================================================================
    // REGIME (principal: ROC_5) — Double
    // 0=BEAR, 1=NEUTRAL, 2=BULL, 3=EXTREME
    // =============================================================================================
    public double calculateRegimeState(ROCIndicator roc, int last) {
        double x = roc.getValue(last).doubleValue();

        // EXTREME (threshold simples; depois você pode trocar por zscore)
        if (Math.abs(x) >= 0.05) return 3.0; // 5% em 5 candles pode ser “extremo” em muitos regimes

        if (x > EPS) return 2.0;
        if (x < -EPS) return 0.0;
        return 1.0;
    }

    /** % últimos window candles com ROC no mesmo sinal do ROC atual */
    public double calculateRegimePersistenceSign(ROCIndicator roc, int last, int window) {
        if (window <= 0) return 0.0;

        int w = Math.min(window, last);
        if (w <= 0) return 0.0;

        double curr = roc.getValue(last).doubleValue();
        if (Math.abs(curr) < EPS) return 0.0;

        int signNow = curr > 0 ? 1 : -1;

        int same = 0;
        for (int i = 0; i < w; i++) {
            double v = roc.getValue(last - i).doubleValue();
            if (Math.abs(v) < EPS) continue;
            int s = v > 0 ? 1 : -1;
            if (s == signNow) same++;
        }

        return same / (double) w;
    }
}
