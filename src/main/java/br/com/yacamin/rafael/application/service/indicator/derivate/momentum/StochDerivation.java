package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.PercentileDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

@Service
@RequiredArgsConstructor
public class StochDerivation {

    private static final double EPS = 1e-12;

    private final ZscoreDerivation zscoreDerivation;
    private final PercentileDerivation percentileDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    // =============================================================================================
    // RAW %K / %D
    // =============================================================================================
    public double calculateK(StochasticOscillatorKIndicator k, int last) {
        return k.getValue(last).doubleValue();
    }

    public double calculateD(StochasticOscillatorDIndicator d, int last) {
        return d.getValue(last).doubleValue();
    }

    // =============================================================================================
    // DELTA
    // =============================================================================================
    public double calculateKDelta(StochasticOscillatorKIndicator k, int last) {
        double curr = k.getValue(last).doubleValue();
        double prev = k.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    public double calculateDDelta(StochasticOscillatorDIndicator d, int last) {
        double curr = d.getValue(last).doubleValue();
        double prev = d.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // SPREAD / CROSS
    // =============================================================================================
    public double calculateSpread(double k, double d) {
        return k - d;
    }

    /** 1 se K>D, -1 se K<D, 0 se igual */
    public double calculateCrossState(double k, double d) {
        double diff = k - d;
        if (diff > EPS) return 1.0;
        if (diff < -EPS) return -1.0;
        return 0.0;
    }

    // =============================================================================================
    // DIST MID (K)
    // =============================================================================================
    public double calculateKDistMid(StochasticOscillatorKIndicator k, int last, double mid) {
        double x = k.getValue(last).doubleValue();
        return Math.abs(x - mid);
    }

    // =============================================================================================
    // ZSCORE / PERCENTILE (K)
    // =============================================================================================
    public double calculateKZscore(StochasticOscillatorKIndicator k, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = k.getValue(last - i).doubleValue();
        }
        return zscoreDerivation.zscore(vals);
    }

    public double calculateKPercentile(StochasticOscillatorKIndicator k, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = k.getValue(last - i).doubleValue();
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    // =============================================================================================
    // SHOCK (ΔK) — bruto e stdn
    // =============================================================================================
    public double calculateKShock1(StochasticOscillatorKIndicator k, int last) {
        return Math.abs(calculateKDelta(k, last));
    }

    public double calculateKShock1Stdn(StochasticOscillatorKIndicator k, int last, int window) {
        if (window <= 2 || last < window) return 0.0;

        int n = window - 1;
        double[] deltas = new double[n];

        for (int i = 0; i < n; i++) {
            double a = k.getValue(last - i).doubleValue();
            double b = k.getValue(last - i - 1).doubleValue();
            deltas[n - 1 - i] = (a - b);
        }

        double sd = stdHelperDerivation.std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    // =============================================================================================
    // REGIME (K_14) — Double
    // 0=OVERSOLD, 1=NEUTRAL, 2=OVERBOUGHT, 3=EXTREME
    // =============================================================================================
    public double calculateKRegimeState(StochasticOscillatorKIndicator k, int last) {
        double x = k.getValue(last).doubleValue(); // 0..100

        // EXTREME
        if (x <= 5.0 || x >= 95.0) return 3.0;

        // OVERSOLD
        if (x <= 20.0) return 0.0;

        // OVERBOUGHT
        if (x >= 80.0) return 2.0;

        // NEUTRAL
        return 1.0;
    }

    /** % últimos window candles no mesmo lado do mid (50) do candle atual */
    public double calculateKRegimePersistenceSide(StochasticOscillatorKIndicator k, int last, int window, double mid) {
        if (window <= 0) return 0.0;

        int w = Math.min(window, last);
        if (w <= 0) return 0.0;

        double curr = k.getValue(last).doubleValue();
        int sideNow = (curr >= mid) ? 1 : -1;

        int same = 0;
        for (int i = 0; i < w; i++) {
            double v = k.getValue(last - i).doubleValue();
            int side = (v >= mid) ? 1 : -1;
            if (side == sideNow) same++;
        }

        return same / (double) w;
    }
}
