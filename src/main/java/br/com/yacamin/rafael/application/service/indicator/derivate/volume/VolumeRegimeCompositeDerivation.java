package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VolumeRegimeCompositeDerivation {

    private static final double EPS = 1e-12;

    // thresholds do score composto
    // 0=LOW, 1=NORMAL, 2=HIGH, 3=EXTREME
    private static final double TH_LOW_MAX     = 0.80;
    private static final double TH_HIGH_MIN    = 1.20;
    private static final double TH_EXTREME_MIN = 1.60;

    // ===== score composto =====
    // - base: avg(volume_rel_48, trades_rel_48)
    // - boost: 0.5 * |ofi_rel_48| + 0.5 * |svr_rel_48|
    // - agressor: taker_buy_ratio centrado em 0.5 (0..1 -> 0..1)
    public double score(double volRel48,
                        double tradesRel48,
                        double takerBuyRatio,
                        double ofiRel48Abs,
                        double svrRel48Abs) {

        double base = (volRel48 + tradesRel48) / 2.0;

        double aggressor = Math.abs(takerBuyRatio - 0.5) * 2.0; // 0..1
        double imbalance = 0.5 * ofiRel48Abs + 0.5 * svrRel48Abs;

        // peso conservador (Sniper): participação manda, imbalance refina
        return base * (1.0 + 0.25 * aggressor) * (1.0 + 0.25 * imbalance);
    }

    public double stateFromScore(double score) {
        if (score >= TH_EXTREME_MIN) return 3.0;
        if (score >= TH_HIGH_MIN)    return 2.0;
        if (score <= TH_LOW_MAX)     return 0.0;
        return 1.0;
    }

    // confiança = distância normalizada até o limiar mais próximo na direção do state
    public double confFromScore(double score) {
        double conf;
        if (score >= TH_EXTREME_MIN) {
            conf = (score - TH_EXTREME_MIN);
        } else if (score >= TH_HIGH_MIN) {
            conf = (score - TH_HIGH_MIN);
        } else if (score <= TH_LOW_MAX) {
            conf = (TH_LOW_MAX - score);
        } else {
            // NEUTRAL: proximidade do centro (quanto mais longe dos limiares, mais confiante)
            double dLow  = score - TH_LOW_MAX;
            double dHigh = TH_HIGH_MIN - score;
            conf = Math.min(dLow, dHigh);
        }

        // normaliza por 0.20 (mesma “granularidade” dos thresholds)
        return conf / 0.20;
    }

    public double[] buildStates(double[] scores) {
        double[] s = new double[scores.length];
        for (int i = 0; i < scores.length; i++) {
            s[i] = stateFromScore(scores[i]);
        }
        return s;
    }

    public double persistence(double[] states, int last, int window) {
        int start = Math.max(0, last - window + 1);
        int n = last - start + 1;

        double cur = states[last];
        int same = 0;
        for (int i = start; i <= last; i++) {
            if (states[i] == cur) same++;
        }
        return (double) same / (double) n;
    }

    public double flipRate(double[] states, int last, int window) {
        int start = Math.max(1, last - window + 1);
        int flips = 0;

        for (int i = start; i <= last; i++) {
            if (states[i] != states[i - 1]) flips++;
        }

        return (double) flips / (double) window;
    }

    // helper
    public double abs(double x) { return Math.abs(x); }
}
