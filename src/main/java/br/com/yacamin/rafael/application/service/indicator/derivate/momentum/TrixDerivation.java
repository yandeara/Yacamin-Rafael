package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.PercentileDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import br.com.yacamin.rafael.application.service.indicator.extension.TrixIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrixDerivation {

    private static final double EPS = 1e-12;

    private final ZscoreDerivation zscoreDerivation;
    private final PercentileDerivation percentileDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    // =============================================================================================
    // RAW / DELTA
    // =============================================================================================
    public double calculateRaw(TrixIndicator trix, int last) {
        return trix.getValue(last).doubleValue();
    }

    public double calculateDelta(TrixIndicator trix, int last) {
        double curr = trix.getValue(last).doubleValue();
        double prev = trix.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // SIGNAL (EMA do TRIX), HIST, CROSS
    // =============================================================================================

    /** EMA(sigPeriod) do TRIX no índice 'last' (calculado “on-the-fly”) */
    public double calculateSignalEma(TrixIndicator trix, int last, int sigPeriod) {
        if (sigPeriod <= 1) return trix.getValue(last).doubleValue();

        double alpha = 2.0 / (sigPeriod + 1.0);

        int start = Math.max(0, last - sigPeriod + 1);

        double ema = trix.getValue(start).doubleValue();
        for (int i = start + 1; i <= last; i++) {
            double x = trix.getValue(i).doubleValue();
            ema = alpha * x + (1.0 - alpha) * ema;
        }

        return ema;
    }

    public double calculateHist(TrixIndicator trix, int last, int sigPeriod) {
        double raw = trix.getValue(last).doubleValue();
        double sig = calculateSignalEma(trix, last, sigPeriod);
        return raw - sig;
    }

    /** 1 se hist>0, -1 se hist<0, 0 se neutro */
    public double calculateCrossState(TrixIndicator trix, int last, int sigPeriod) {
        double h = calculateHist(trix, last, sigPeriod);
        if (h > EPS) return 1.0;
        if (h < -EPS) return -1.0;
        return 0.0;
    }

    // =============================================================================================
    // ZSCORE / PERCENTILE (TRIX raw)
    // =============================================================================================

    public double calculateZscore(TrixIndicator trix, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = trix.getValue(last - i).doubleValue();
        }
        return zscoreDerivation.zscore(vals);
    }

    public double calculatePercentile(TrixIndicator trix, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = trix.getValue(last - i).doubleValue();
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    // =============================================================================================
    // SHOCK (ΔTRIX) — bruto + stdn
    // =============================================================================================

    public double calculateShock1(TrixIndicator trix, int last) {
        return Math.abs(calculateDelta(trix, last));
    }

    public double calculateShock1Stdn(TrixIndicator trix, int last, int window) {
        if (window <= 2 || last < window) return 0.0;

        int n = window - 1;
        double[] deltas = new double[n];

        for (int i = 0; i < n; i++) {
            double a = trix.getValue(last - i).doubleValue();
            double b = trix.getValue(last - i - 1).doubleValue();
            deltas[n - 1 - i] = (a - b);
        }

        double sd = stdHelperDerivation.std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    // =============================================================================================
    // REGIME (TRIX_9) — baseado no hist (sigPeriod=9)
    // 0=BEAR, 1=NEUTRAL, 2=BULL, 3=EXTREME
    // =============================================================================================

    public double calculateRegimeState(TrixIndicator trix, int last, int sigPeriod) {
        double h = calculateHist(trix, last, sigPeriod);

        // EXTREME: hist muito grande em módulo (normalizado via stdn fica melhor, mas aqui é “rápido”)
        if (Math.abs(h) >= 0.5) return 3.0; // threshold conservador (ajustável)

        if (h > EPS) return 2.0;      // BULL
        if (h < -EPS) return 0.0;     // BEAR
        return 1.0;                   // NEUTRAL
    }

    /** % últimos window candles com hist no mesmo sinal do hist atual */
    public double calculateRegimePersistence(TrixIndicator trix, int last, int window, int sigPeriod) {
        if (window <= 0) return 0.0;

        int w = Math.min(window, last);
        if (w <= 0) return 0.0;

        double hNow = calculateHist(trix, last, sigPeriod);
        if (Math.abs(hNow) < EPS) return 0.0;

        int signNow = hNow > 0 ? 1 : -1;

        int same = 0;
        for (int i = 0; i < w; i++) {
            double h = calculateHist(trix, last - i, sigPeriod);
            if (Math.abs(h) < EPS) continue;
            int s = h > 0 ? 1 : -1;
            if (s == signNow) same++;
        }

        return same / (double) w;
    }
}
