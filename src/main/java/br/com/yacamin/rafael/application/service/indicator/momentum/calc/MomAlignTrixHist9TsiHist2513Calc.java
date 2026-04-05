package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomAlignTrixHist9TsiHist2513Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double trixHist9, double tsiHist25) {
        return sign(trixHist9) * sign(tsiHist25);
    }

    private static double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_align_trix_hist_9_tsi_hist_25_13",
                "Alignment TRIX Hist(9) vs TSI Hist(25,13)",
                "momentum",
                "sign(TRIX_hist(9)) * sign(TSI_hist(25,13))",
                "Alinhamento entre histogramas TRIX(9) e TSI(25,13). +1=bullish, -1=bearish, 0=neutro.",
                "{-1, 0, +1}",
                "TRIX_hist>0 -> +1, TSI_hist>0 -> +1; resultado=+1"
        );
    }
}
