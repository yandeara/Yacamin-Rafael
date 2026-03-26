package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.PercentileDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import br.com.yacamin.rafael.application.service.indicator.extension.TsiIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TsiDerivation {

    private static final double EPS = 1e-12;

    private final ZscoreDerivation zscoreDerivation;
    private final PercentileDerivation percentileDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    // =============================================================================================
    // RAW / DELTA
    // =============================================================================================
    public double calculateRaw(TsiIndicator tsi, int last) {
        return tsi.getValue(last).doubleValue();
    }

    public double calculateDelta(TsiIndicator tsi, int last) {
        double curr = tsi.getValue(last).doubleValue();
        double prev = tsi.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // SIGNAL (EMA do TSI), HIST, CROSS
    // =============================================================================================

    /** EMA(sigPeriod) do TSI no índice 'last' (calculado “on-the-fly”) */
    public double calculateSignalEma(TsiIndicator tsi, int last, int sigPeriod) {
        if (sigPeriod <= 1) return tsi.getValue(last).doubleValue();

        double alpha = 2.0 / (sigPeriod + 1.0);
        int start = Math.max(0, last - sigPeriod + 1);

        double ema = tsi.getValue(start).doubleValue();
        for (int i = start + 1; i <= last; i++) {
            double x = tsi.getValue(i).doubleValue();
            ema = alpha * x + (1.0 - alpha) * ema;
        }

        return ema;
    }

    public double calculateHist(TsiIndicator tsi, int last, int sigPeriod) {
        double raw = tsi.getValue(last).doubleValue();
        double sig = calculateSignalEma(tsi, last, sigPeriod);
        return raw - sig;
    }

    /** 1 se hist>0, -1 se hist<0, 0 se neutro */
    public double calculateCrossState(TsiIndicator tsi, int last, int sigPeriod) {
        double h = calculateHist(tsi, last, sigPeriod);
        if (h > EPS) return 1.0;
        if (h < -EPS) return -1.0;
        return 0.0;
    }

    // =============================================================================================
    // DIST MID = |TSI|
    // =============================================================================================
    public double calculateDistMid(TsiIndicator tsi, int last) {
        return Math.abs(tsi.getValue(last).doubleValue());
    }

    // =============================================================================================
    // ZSCORE / PERCENTILE (TSI raw)
    // =============================================================================================

    public double calculateZscore(TsiIndicator tsi, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = tsi.getValue(last - i).doubleValue();
        }
        return zscoreDerivation.zscore(vals);
    }

    public double calculatePercentile(TsiIndicator tsi, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = tsi.getValue(last - i).doubleValue();
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    // =============================================================================================
    // SHOCK (|ΔTSI|) — bruto + stdn
    // =============================================================================================

    public double calculateShock1(TsiIndicator tsi, int last) {
        return Math.abs(calculateDelta(tsi, last));
    }

    public double calculateShock1Stdn(TsiIndicator tsi, int last, int window) {
        if (window <= 2 || last < window) return 0.0;

        int n = window - 1;
        double[] deltas = new double[n];

        for (int i = 0; i < n; i++) {
            double a = tsi.getValue(last - i).doubleValue();
            double b = tsi.getValue(last - i - 1).doubleValue();
            deltas[n - 1 - i] = (a - b);
        }

        double sd = stdHelperDerivation.std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    // =============================================================================================
    // REGIME (principal: 25/13) — baseado no hist (sigPeriod=7)
    // 0=BEAR, 1=NEUTRAL, 2=BULL, 3=EXTREME
    // =============================================================================================

    public double calculateRegimeState(TsiIndicator tsi, int last, int sigPeriod) {
        double h = calculateHist(tsi, last, sigPeriod);

        // EXTREME: hist grande em módulo (threshold conservador; ajuste depois)
        if (Math.abs(h) >= 5.0) return 3.0;

        if (h > EPS) return 2.0;      // BULL
        if (h < -EPS) return 0.0;     // BEAR
        return 1.0;                   // NEUTRAL
    }

    /** % últimos window candles com hist no mesmo sinal do hist atual */
    public double calculateRegimePersistence(TsiIndicator tsi, int last, int window, int sigPeriod) {
        if (window <= 0) return 0.0;

        int w = Math.min(window, last);
        if (w <= 0) return 0.0;

        double hNow = calculateHist(tsi, last, sigPeriod);
        if (Math.abs(hNow) < EPS) return 0.0;

        int signNow = hNow > 0 ? 1 : -1;

        int same = 0;
        for (int i = 0; i < w; i++) {
            double h = calculateHist(tsi, last - i, sigPeriod);
            if (Math.abs(h) < EPS) continue;
            int s = h > 0 ? 1 : -1;
            if (s == signNow) same++;
        }

        return same / (double) w;
    }
}
