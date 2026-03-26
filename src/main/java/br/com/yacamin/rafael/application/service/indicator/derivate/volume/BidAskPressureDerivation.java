package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import br.com.yacamin.rafael.domain.RafaelBar;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
public class BidAskPressureDerivation {

    private static final double EPS = 1e-12;

    /** BAP(t) = (close - open) / volume */
    public double bap(BarSeries series, int index) {
        RafaelBar bar = (RafaelBar) series.getBar(index);

        double open  = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double vol   = bar.getVolume().doubleValue();

        return (close - open) / vol;
    }

    /** slope (linear regression) over last N BAP values */
    public double bapSlope(BarSeries series, int index, int window) {
        double[] y = new double[window];
        for (int i = 0; i < window; i++) {
            y[i] = bap(series, index - i);
        }
        return computeSlope(y);
    }

    /** acceleration = slope(t) - slope(t-1) */
    public double bapAcceleration(BarSeries series, int index, int window) {
        double slopeNow  = bapSlope(series, index, window);
        double slopePrev = bapSlope(series, index - 1, window);
        return slopeNow - slopePrev;
    }

    // =========================================================================
    // NEW: REL / ZSCORE / FLIP / PRST / VOV
    // =========================================================================

    /** REL_16 = bap_now / mean(|bap|) in window */
    public double bapRel(BarSeries series, int index, int window) {
        double now = bap(series, index);

        int start = index - window + 1;
        double sumAbs = 0.0;

        for (int i = start; i <= index; i++) {
            sumAbs += Math.abs(bap(series, i));
        }

        double meanAbs = sumAbs / window;
        if (meanAbs < EPS) return 0.0;

        return now / meanAbs;
    }

    /** ZSCORE window (classic) */
    public double bapZscore(BarSeries series, int index, int window) {
        int start = index - window + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = bap(series, i);
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / window;
        double var = (sumSq / window) - (mean * mean);
        if (var < 0 && var > -1e-9) var = 0.0;

        double sd = Math.sqrt(var);
        if (sd < EPS) return 0.0;

        double last = bap(series, index);
        return (last - mean) / sd;
    }

    /** Vol-of-vol (std) of BAP values over window */
    public double bapVolOfVol(BarSeries series, int index, int window) {
        int start = index - window + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = bap(series, i);
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / window;
        double var = (sumSq / window) - (mean * mean);
        if (var < 0 && var > -1e-9) var = 0.0;

        return Math.sqrt(var);
    }

    private double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    /** Flip rate w20: mudanças de sinal do BAP */
    public double bapFlipRateW20(BarSeries series, int index) {
        int window = 20;
        int start = index - window + 1;
        if (start < 1) return 0.0;

        int flips = 0;
        double prev = sign(bap(series, start - 1));

        for (int i = start; i <= index; i++) {
            double cur = sign(bap(series, i));
            if (cur != 0.0 && prev != 0.0 && cur != prev) flips++;
            if (cur != 0.0) prev = cur;
        }

        return (double) flips / (double) window;
    }

    /** Persistence w20: % dos últimos 20 com mesmo sinal do BAP atual */
    public double bapPrstW20(BarSeries series, int index) {
        int window = 20;
        int start = index - window + 1;
        if (start < 0) return 0.0;

        double now = sign(bap(series, index));
        if (now == 0.0) return 0.0;

        int same = 0;
        for (int i = start; i <= index; i++) {
            if (sign(bap(series, i)) == now) same++;
        }

        return (double) same / (double) window;
    }

    // =========================================================================
    // regression helper
    // =========================================================================
    private double computeSlope(double[] y) {
        int n = y.length;

        double sumX  = 0.0;
        double sumY  = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double yi = y[i];

            sumX  += x;
            sumY  += yi;
            sumXY += x * yi;
            sumX2 += x * x;
        }

        double numerator   = n * sumXY - (sumX * sumY);
        double denominator = n * sumX2 - (sumX * sumX);

        return numerator / denominator;
    }
}
