package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MomentumStabilityDerivation {

    private static final double EPS = 1e-12;

    private double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    /** 1 - |consensus| (0=alinhado, 1=conflito) */
    public double conflictScore(double consensus) {
        double v = 1.0 - Math.abs(consensus);
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }

    public double delta(double now, double prev) {
        return now - prev;
    }

    /**
     * Persistência do consenso: % na janela com sign(cons) == sign(cons_now).
     * Entrada: vals em ordem oldest -> newest.
     */
    public double consensusPersistenceSameSign(double[] vals) {
        if (vals == null || vals.length == 0) return 0.0;

        double sNow = sign(vals[vals.length - 1]);
        if (sNow == 0.0) return 0.0;

        int same = 0;
        for (double v : vals) {
            if (sign(v) == sNow) same++;
        }
        return same / (double) vals.length;
    }

    /**
     * Flip rate: frequência de mudança de sinal na janela (ignora zeros),
     * normalizado por (len-1).
     * Entrada: vals em ordem oldest -> newest.
     */
    public double flipRate(double[] vals) {
        if (vals == null || vals.length < 2) return 0.0;

        int flips = 0;
        double prevSign = 0.0;

        // pega primeiro sinal não-zero
        for (double v : vals) {
            double s = sign(v);
            if (s != 0.0) {
                prevSign = s;
                break;
            }
        }

        // se todos foram 0
        if (prevSign == 0.0) return 0.0;

        for (int i = 1; i < vals.length; i++) {
            double s = sign(vals[i]);

            if (s != 0.0 && prevSign != 0.0 && s != prevSign) {
                flips++;
            }
            if (s != 0.0) {
                prevSign = s;
            }
        }

        return flips / (double) (vals.length - 1);
    }

    // =============================================================================================
    // SLOPE (regressão linear simples) — vals em ordem oldest -> newest
    // x = 0..n-1, y = vals[i]
    // =============================================================================================
    public double slope(double[] vals) {
        if (vals == null || vals.length < 2) return 0.0;

        int n = vals.length;

        double sumX  = 0.0;
        double sumY  = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = vals[i];

            sumX  += x;
            sumY  += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);
        if (Math.abs(denom) < EPS) return 0.0;

        return (n * sumXY - sumX * sumY) / denom;
    }

    // =============================================================================================
    // CONSENSUS STATS (NOVOS)
    // =============================================================================================

    /** Std simples (janela). */
    public double std(double[] vals) {
        if (vals == null || vals.length < 2) return 0.0;

        double mean = 0.0;
        for (double v : vals) mean += v;
        mean /= vals.length;

        double var = 0.0;
        for (double v : vals) {
            double d = v - mean;
            var += d * d;
        }
        var /= vals.length;

        double sd = Math.sqrt(var);
        if (Double.isNaN(sd) || Double.isInfinite(sd)) return 0.0;
        return sd;
    }

    /** Shock instantâneo do consenso: |Δcons| */
    public double consensusShock1(double consensusNow, double consensusPrev) {
        return Math.abs(consensusNow - consensusPrev);
    }

    /**
     * Shock normalizado por vol(Δconsensus) na janela.
     * Entrada: consensusWindow em ordem oldest -> newest (len>=2).
     * Retorno: |Δcons_last| / std(Δcons_window)
     */
    public double consensusShock1Stdn(double[] consensusWindow) {
        if (consensusWindow == null || consensusWindow.length < 3) return 0.0;

        int n = consensusWindow.length - 1; // deltas
        double[] deltas = new double[n];

        for (int i = 0; i < n; i++) {
            double now  = consensusWindow[i + 1];
            double prev = consensusWindow[i];
            deltas[i] = now - prev;
        }

        double sd = std(deltas);
        if (sd < EPS) return 0.0;

        double shock = Math.abs(deltas[n - 1]);
        return shock / sd;
    }

    /** Conta quantos alignments == +1 (ignora 0). */
    public double agreementCount(double... aligns) {
        if (aligns == null || aligns.length == 0) return 0.0;
        int c = 0;
        for (double a : aligns) {
            if (a > EPS) c++;
        }
        return (double) c;
    }

    /** Conta quantos alignments == -1 (ignora 0). */
    public double disagreementCount(double... aligns) {
        if (aligns == null || aligns.length == 0) return 0.0;
        int c = 0;
        for (double a : aligns) {
            if (a < -EPS) c++;
        }
        return (double) c;
    }
}
