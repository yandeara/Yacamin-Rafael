package br.com.yacamin.rafael.application.service.indicator.derivate.momentum;

import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.PpoDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.TrixDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.TsiDerivation;
import br.com.yacamin.rafael.application.service.indicator.extension.TrixIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.TsiIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

@Service
@RequiredArgsConstructor
public class MomentumConsensusDerivation {

    private static final double EPS = 1e-12;

    private static final int W20 = 20;
    private static final int W80 = 80;

    private static final int TRIX_SIG = 9;
    private static final int TSI_SIG  = 7;

    private final PpoDerivation ppoDerivation;
    private final TrixDerivation trixDerivation;
    private final TsiDerivation tsiDerivation;
    private final MomentumStabilityDerivation stats; // seu "math lib"

    // ==========================
    // Snapshot (pacote de features)
    // ==========================
    public static class Snapshot {
        // Base (alinhamentos + consensus + chop)
        public double align_rsi14_ppo12;
        public double align_rsi48_ppo48;
        public double align_rsi288_ppo288;
        public double align_trix9_tsi25;
        public double align_trix48_tsi48;
        public double align_trix288_tsi288;

        public double consensus_now;
        public double chop_score;

        // Mini-subgrupo (stability/consensus dynamics)
        public double momentum_conflict_score;
        public double momentum_consensus_dlt;
        public double momentum_consensus_prst_w20;

        public double ppo_hist_12_flip_rate_w20;
        public double ppo_hist_48_flip_rate_w20;
        public double ppo_hist_288_flip_rate_w20;

        public double trix_hist_9_flip_rate_w20;
        public double trix_hist_48_flip_rate_w20;
        public double trix_hist_288_flip_rate_w20;

        public double tsi_hist_25_flip_rate_w20;
        public double tsi_hist_48_flip_rate_w20;
        public double tsi_hist_288_flip_rate_w20;

        public double ppo_hist_coh_12_vs_48;
        public double ppo_hist_coh_12_vs_288;
        public double trix_hist_coh_9_vs_48;
        public double tsi_hist_coh_25_vs_48;

        public double consensus_slp_w20;
        public double consensus_flip_rate_w20;

        public double div_close_slp48_vs_ppo_hist48;
        public double div_close_slp48_vs_rsi48;
        public double div_close_slp288_vs_ppo_hist288;

        public double consensus_vol_w20;
        public double consensus_shock_1;
        public double consensus_shock_1_stdn_w20;

        public double disagreement_count;
        public double agreement_count;

        // Consensus quality
        public double consensus_abs;
        public double neutral_count;
        public double vote_entropy;
        public double consensus_run_len;
        public double signal_quality_score;

        // Drifts
        public double ppo_hist_12_slp_w20;
        public double ppo_hist_48_slp_w20;
        public double ppo_hist_288_slp_w20;

        public double trix_hist_9_slp_w20;
        public double trix_hist_48_slp_w20;
        public double trix_hist_288_slp_w20;

        public double tsi_hist_25_slp_w20;
        public double tsi_hist_48_slp_w20;
        public double tsi_hist_288_slp_w20;

        // Extremes (history)
        public double consensus_zsc_80;
        public double consensus_pctile_w80;
    }

    // =============================================================================================
    // API principal: calcula tudo em um pacote
    // =============================================================================================
    public Snapshot compute(BarSeries series,
                            int index,
                            RSIIndicator rsi14, RSIIndicator rsi48, RSIIndicator rsi288,
                            PPOIndicator ppo12, EMAIndicator sig12,
                            PPOIndicator ppo48, EMAIndicator sig48,
                            PPOIndicator ppo288, EMAIndicator sig288,
                            TrixIndicator trix9, TrixIndicator trix48, TrixIndicator trix288,
                            TsiIndicator tsi25_13, TsiIndicator tsi48_25, TsiIndicator tsi288_144,
                            double chopScore) {

        Snapshot out = new Snapshot();
        out.chop_score = chopScore;

        // ----------------------------
        // Build W20 series (hist + consensus)
        // ----------------------------
        int w20 = Math.min(W20, index);
        int start20 = index - w20 + 1;

        double[] ppo12W  = new double[w20];
        double[] ppo48W  = new double[w20];
        double[] ppo288W = new double[w20];

        double[] trix9W   = new double[w20];
        double[] trix48W  = new double[w20];
        double[] trix288W = new double[w20];

        double[] tsi25W   = new double[w20];
        double[] tsi48W   = new double[w20];
        double[] tsi288W  = new double[w20];

        double[] consensusW20 = new double[w20];

        int p = 0;
        for (int i = start20; i <= index; i++) {
            double h12  = ppoDerivation.calculateHistogram(ppo12,  sig12,  i);
            double h48  = ppoDerivation.calculateHistogram(ppo48,  sig48,  i);
            double h288 = ppoDerivation.calculateHistogram(ppo288, sig288, i);

            double th9   = trixDerivation.calculateHist(trix9,   i, TRIX_SIG);
            double th48  = trixDerivation.calculateHist(trix48,  i, TRIX_SIG);
            double th288 = trixDerivation.calculateHist(trix288, i, TRIX_SIG);

            double sh25  = tsiDerivation.calculateHist(tsi25_13,   i, TSI_SIG);
            double sh48  = tsiDerivation.calculateHist(tsi48_25,   i, TSI_SIG);
            double sh288 = tsiDerivation.calculateHist(tsi288_144, i, TSI_SIG);

            ppo12W[p]  = h12;
            ppo48W[p]  = h48;
            ppo288W[p] = h288;

            trix9W[p]   = th9;
            trix48W[p]  = th48;
            trix288W[p] = th288;

            tsi25W[p]  = sh25;
            tsi48W[p]  = sh48;
            tsi288W[p] = sh288;

            // alignments do candle
            double a1 = alignment((rsi14.getValue(i).doubleValue() - 50.0), h12);
            double a2 = alignment((rsi48.getValue(i).doubleValue() - 50.0), h48);
            double a3 = alignment((rsi288.getValue(i).doubleValue() - 50.0), h288);

            double a4 = alignment(th9,  sh25);
            double a5 = alignment(th48, sh48);
            double a6 = alignment(th288, sh288);

            consensusW20[p] = (a1 + a2 + a3 + a4 + a5 + a6) / 6.0;

            p++;
        }

        // Now values (último candle)
        double ppo12Now  = ppo12W[w20 - 1];
        double ppo48Now  = ppo48W[w20 - 1];
        double ppo288Now = ppo288W[w20 - 1];

        double trix9Now   = trix9W[w20 - 1];
        double trix48Now  = trix48W[w20 - 1];
        double trix288Now = trix288W[w20 - 1];

        double tsi25Now  = tsi25W[w20 - 1];
        double tsi48Now  = tsi48W[w20 - 1];
        double tsi288Now = tsi288W[w20 - 1];

        double rsi14Now  = rsi14.getValue(index).doubleValue();
        double rsi48Now  = rsi48.getValue(index).doubleValue();
        double rsi288Now = rsi288.getValue(index).doubleValue();

        out.align_rsi14_ppo12 = alignment((rsi14Now - 50.0), ppo12Now);
        out.align_rsi48_ppo48 = alignment((rsi48Now - 50.0), ppo48Now);
        out.align_rsi288_ppo288 = alignment((rsi288Now - 50.0), ppo288Now);

        out.align_trix9_tsi25 = alignment(trix9Now, tsi25Now);
        out.align_trix48_tsi48 = alignment(trix48Now, tsi48Now);
        out.align_trix288_tsi288 = alignment(trix288Now, tsi288Now);

        out.consensus_now = (out.align_rsi14_ppo12 + out.align_rsi48_ppo48 + out.align_rsi288_ppo288
                + out.align_trix9_tsi25 + out.align_trix48_tsi48 + out.align_trix288_tsi288) / 6.0;

        double consensusPrev = (w20 >= 2) ? consensusW20[w20 - 2] : out.consensus_now;

        // ----------------------------
        // Core stability
        // ----------------------------
        out.momentum_conflict_score = stats.conflictScore(out.consensus_now);
        out.momentum_consensus_dlt = stats.delta(out.consensus_now, consensusPrev);
        out.momentum_consensus_prst_w20 = stats.consensusPersistenceSameSign(consensusW20);

        out.ppo_hist_12_flip_rate_w20 = stats.flipRate(ppo12W);
        out.ppo_hist_48_flip_rate_w20 = stats.flipRate(ppo48W);
        out.ppo_hist_288_flip_rate_w20 = stats.flipRate(ppo288W);

        out.trix_hist_9_flip_rate_w20 = stats.flipRate(trix9W);
        out.trix_hist_48_flip_rate_w20 = stats.flipRate(trix48W);
        out.trix_hist_288_flip_rate_w20 = stats.flipRate(trix288W);

        out.tsi_hist_25_flip_rate_w20 = stats.flipRate(tsi25W);
        out.tsi_hist_48_flip_rate_w20 = stats.flipRate(tsi48W);
        out.tsi_hist_288_flip_rate_w20 = stats.flipRate(tsi288W);

        // coherence
        out.ppo_hist_coh_12_vs_48 = alignment(ppo12Now, ppo48Now);
        out.ppo_hist_coh_12_vs_288 = alignment(ppo12Now, ppo288Now);
        out.trix_hist_coh_9_vs_48 = alignment(trix9Now, trix48Now);
        out.tsi_hist_coh_25_vs_48 = alignment(tsi25Now, tsi48Now);

        // consensus slope + flip
        out.consensus_slp_w20 = stats.slope(consensusW20);
        out.consensus_flip_rate_w20 = stats.flipRate(consensusW20);

        // divergence price vs momentum (usa slope simples do close: delta/len)
        double closeSlp48  = closeSlopeSimple(series, index, 48);
        double closeSlp288 = closeSlopeSimple(series, index, 288);

        out.div_close_slp48_vs_ppo_hist48 = alignment(closeSlp48, ppo48Now);
        out.div_close_slp48_vs_rsi48      = alignment(closeSlp48, (rsi48Now - 50.0));
        out.div_close_slp288_vs_ppo_hist288 = alignment(closeSlp288, ppo288Now);

        // consensus stats
        out.consensus_vol_w20 = stats.std(consensusW20);
        out.consensus_shock_1 = stats.consensusShock1(out.consensus_now, consensusPrev);
        out.consensus_shock_1_stdn_w20 = stats.consensusShock1Stdn(consensusW20);

        out.agreement_count = stats.agreementCount(
                out.align_rsi14_ppo12, out.align_rsi48_ppo48, out.align_rsi288_ppo288,
                out.align_trix9_tsi25, out.align_trix48_tsi48, out.align_trix288_tsi288
        );
        out.disagreement_count = stats.disagreementCount(
                out.align_rsi14_ppo12, out.align_rsi48_ppo48, out.align_rsi288_ppo288,
                out.align_trix9_tsi25, out.align_trix48_tsi48, out.align_trix288_tsi288
        );

        // consensus quality
        out.consensus_abs = Math.abs(out.consensus_now);
        out.neutral_count = 6.0 - out.agreement_count - out.disagreement_count;
        out.vote_entropy = voteEntropyNormalized(out.agreement_count, out.disagreement_count, out.neutral_count);
        out.consensus_run_len = consensusRunLen(consensusW20);

        out.signal_quality_score = clamp01(
                out.consensus_abs
                        * (1.0 - out.consensus_flip_rate_w20)
                        * (1.0 - out.chop_score)
        );

        // drifts (w20 slopes)
        out.ppo_hist_12_slp_w20 = stats.slope(ppo12W);
        out.ppo_hist_48_slp_w20 = stats.slope(ppo48W);
        out.ppo_hist_288_slp_w20 = stats.slope(ppo288W);

        out.trix_hist_9_slp_w20 = stats.slope(trix9W);
        out.trix_hist_48_slp_w20 = stats.slope(trix48W);
        out.trix_hist_288_slp_w20 = stats.slope(trix288W);

        out.tsi_hist_25_slp_w20 = stats.slope(tsi25W);
        out.tsi_hist_48_slp_w20 = stats.slope(tsi48W);
        out.tsi_hist_288_slp_w20 = stats.slope(tsi288W);

        // ----------------------------
        // Extremes (W80) — consensus zscore/pctile
        // ----------------------------
        int w80 = Math.min(W80, index);
        int start80 = index - w80 + 1;

        double[] consensusW80 = new double[w80];
        int q = 0;
        for (int i = start80; i <= index; i++) {

            double h12  = ppoDerivation.calculateHistogram(ppo12,  sig12,  i);
            double h48  = ppoDerivation.calculateHistogram(ppo48,  sig48,  i);
            double h288 = ppoDerivation.calculateHistogram(ppo288, sig288, i);

            double th9   = trixDerivation.calculateHist(trix9,   i, TRIX_SIG);
            double th48  = trixDerivation.calculateHist(trix48,  i, TRIX_SIG);
            double th288 = trixDerivation.calculateHist(trix288, i, TRIX_SIG);

            double sh25  = tsiDerivation.calculateHist(tsi25_13,   i, TSI_SIG);
            double sh48  = tsiDerivation.calculateHist(tsi48_25,   i, TSI_SIG);
            double sh288 = tsiDerivation.calculateHist(tsi288_144, i, TSI_SIG);

            double a1 = alignment((rsi14.getValue(i).doubleValue() - 50.0), h12);
            double a2 = alignment((rsi48.getValue(i).doubleValue() - 50.0), h48);
            double a3 = alignment((rsi288.getValue(i).doubleValue() - 50.0), h288);

            double a4 = alignment(th9,  sh25);
            double a5 = alignment(th48, sh48);
            double a6 = alignment(th288, sh288);

            consensusW80[q++] = (a1 + a2 + a3 + a4 + a5 + a6) / 6.0;
        }

        out.consensus_zsc_80 = zScore(consensusW80);
        out.consensus_pctile_w80 = pctileRank(consensusW80);

        return out;
    }

    // =============================================================================================
    // Helpers
    // =============================================================================================
    private double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    /** sign(a) * sign(b) */
    private double alignment(double a, double b) {
        return sign(a) * sign(b);
    }

    private double closeSlopeSimple(BarSeries series, int index, int window) {
        int past = index - window;
        if (past < 0) return 0.0;
        double cNow = series.getBar(index).getClosePrice().doubleValue();
        double cPast = series.getBar(past).getClosePrice().doubleValue();
        return (cNow - cPast) / (double) window;
    }

    private double consensusRunLen(double[] consW) {
        if (consW == null || consW.length == 0) return 0.0;

        double sNow = sign(consW[consW.length - 1]);
        if (sNow == 0.0) return 0.0;

        int len = 0;
        for (int i = consW.length - 1; i >= 0; i--) {
            double s = sign(consW[i]);
            if (s != sNow) break;
            len++;
        }
        return (double) len;
    }

    // entropy normalizada (0..1) em 3 classes: agree/disagree/neutral
    private double voteEntropyNormalized(double agree, double disagree, double neutral) {
        double total = Math.max(1.0, agree + disagree + neutral);

        double pa = agree / total;
        double pd = disagree / total;
        double pn = neutral / total;

        double e = 0.0;
        e -= (pa > 0) ? pa * Math.log(pa) : 0.0;
        e -= (pd > 0) ? pd * Math.log(pd) : 0.0;
        e -= (pn > 0) ? pn * Math.log(pn) : 0.0;

        double max = Math.log(3.0);
        return (max > 0) ? (e / max) : 0.0;
    }

    private double zScore(double[] x) {
        if (x == null || x.length < 2) return 0.0;

        double sum = 0.0, sumSq = 0.0;
        for (double v : x) { sum += v; sumSq += v * v; }
        double mean = sum / x.length;
        double var = (sumSq / x.length) - (mean * mean);
        double sd = Math.sqrt(var);

        double last = x[x.length - 1];
        return (last - mean) / sd;
    }

    private double pctileRank(double[] x) {
        if (x == null || x.length == 0) return 0.0;

        double cur = x[x.length - 1];
        int le = 0;
        for (double v : x) if (v <= cur) le++;
        return (double) le / (double) x.length;
    }

    private double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}
