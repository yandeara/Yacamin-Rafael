package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.helper.PercentileDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.StdHelperDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.ZscoreDerivation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Service
@RequiredArgsConstructor
public class PpoDerivation {

    private static final double EPS = 1e-12;

    private final ZscoreDerivation zscoreDerivation;
    private final PercentileDerivation percentileDerivation;
    private final StdHelperDerivation stdHelperDerivation;

    // =============================================================================================
    // RAW / SIGNAL / HIST
    // =============================================================================================
    public double calculateRaw(PPOIndicator ppo, int last) {
        return ppo.getValue(last).doubleValue();
    }

    public double calculateSignal(EMAIndicator signal, int last) {
        return signal.getValue(last).doubleValue();
    }

    public double calculateHistogram(PPOIndicator ppo, EMAIndicator signal, int last) {
        double p = ppo.getValue(last).doubleValue();
        double s = signal.getValue(last).doubleValue();
        return p - s;
    }

    // =============================================================================================
    // DELTAS
    // =============================================================================================
    public double calculateDelta(PPOIndicator ppo, int last) {
        double curr = ppo.getValue(last).doubleValue();
        double prev = ppo.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    public double calculateHistogramDelta(PPOIndicator ppo, EMAIndicator signal, int last) {
        double hNow = calculateHistogram(ppo, signal, last);
        double hPrev = calculateHistogram(ppo, signal, last - 1);
        return hNow - hPrev;
    }

    // =============================================================================================
    // ZSCORE / PERCENTILE (window)
    // =============================================================================================
    public double calculateZscorePpo(PPOIndicator ppo, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = ppo.getValue(last - i).doubleValue();
        }
        return zscoreDerivation.zscore(vals);
    }

    public double calculatePercentilePpo(PPOIndicator ppo, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            vals[window - 1 - i] = ppo.getValue(last - i).doubleValue();
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    public double calculateZscoreHist(PPOIndicator ppo, EMAIndicator signal, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            int idx = last - i;
            vals[window - 1 - i] = calculateHistogram(ppo, signal, idx);
        }
        return zscoreDerivation.zscore(vals);
    }

    public double calculatePercentileHist(PPOIndicator ppo, EMAIndicator signal, int last, int window) {
        if (window <= 1 || last < window) return 0.0;

        double[] vals = new double[window];
        for (int i = 0; i < window; i++) {
            int idx = last - i;
            vals[window - 1 - i] = calculateHistogram(ppo, signal, idx);
        }
        double current = vals[window - 1];
        return percentileDerivation.percentileRank(vals, current);
    }

    // =============================================================================================
    // SHOCK (|ΔHIST|) + SHOCK_STDN(window)
    // =============================================================================================
    public double calculateHistShock1(PPOIndicator ppo, EMAIndicator signal, int last) {
        return Math.abs(calculateHistogramDelta(ppo, signal, last));
    }

    public double calculateHistShock1Stdn(PPOIndicator ppo, EMAIndicator signal, int last, int window) {
        if (window <= 2 || last < window) return 0.0;

        int n = window - 1;
        double[] deltas = new double[n];

        for (int i = 0; i < n; i++) {
            int idx = last - i;
            double hNow  = calculateHistogram(ppo, signal, idx);
            double hPrev = calculateHistogram(ppo, signal, idx - 1);
            deltas[n - 1 - i] = (hNow - hPrev);
        }

        double sd = stdHelperDerivation.std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    // =============================================================================================
    // REGIME (hist) + PERSISTENCE (w20) — Double
    // 0=BEAR, 1=NEUTRAL, 2=BULL, 3=EXTREME
    // =============================================================================================
    public double calculateRegimeState(PPOIndicator ppo, EMAIndicator signal, int last) {
        double h = calculateHistogram(ppo, signal, last);

        // EXTREME por zscore do hist (mais robusto que threshold absoluto)
        // Se você não quiser “recalcular” zscore aqui, deixe simples. Aqui usei threshold absoluto conservador.
        if (Math.abs(h) >= 1.0) return 3.0;

        if (h > EPS) return 2.0;
        if (h < -EPS) return 0.0;
        return 1.0;
    }

    /** % últimos window candles com hist no mesmo sinal do hist atual */
    public double calculateRegimePersistence(PPOIndicator ppo, EMAIndicator signal, int last, int window) {
        if (window <= 0) return 0.0;

        int w = Math.min(window, last);
        if (w <= 0) return 0.0;

        double hNow = calculateHistogram(ppo, signal, last);
        if (Math.abs(hNow) < EPS) return 0.0;

        int signNow = hNow > 0 ? 1 : -1;

        int same = 0;
        for (int i = 0; i < w; i++) {
            double h = calculateHistogram(ppo, signal, last - i);
            if (Math.abs(h) < EPS) continue;
            int s = h > 0 ? 1 : -1;
            if (s == signNow) same++;
        }

        return same / (double) w;
    }
}
