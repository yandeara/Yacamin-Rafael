package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomAlignTrixHist48TsiHist4825Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double trixHist48, double tsiHist48) {
        return sign(trixHist48) * sign(tsiHist48);
    }

    private static double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_align_trix_hist_48_tsi_hist_48_25",
                "Alignment TRIX Hist(48) vs TSI Hist(48,25)",
                "momentum",
                "sign(TRIX_hist(48)) * sign(TSI_hist(48,25))",
                "Alinhamento entre histogramas TRIX(48) e TSI(48,25). +1=bullish, -1=bearish, 0=neutro.",
                "{-1, 0, +1}",
                "TRIX_hist>0 -> +1, TSI_hist<0 -> -1; resultado=-1"
        );
    }
}
