package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomAlignTrixHist288TsiHist288144Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double trixHist288, double tsiHist288) {
        return sign(trixHist288) * sign(tsiHist288);
    }

    private static double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_align_trix_hist_288_tsi_hist_288_144",
                "Alignment TRIX Hist(288) vs TSI Hist(288,144)",
                "momentum",
                "sign(TRIX_hist(288)) * sign(TSI_hist(288,144))",
                "Alinhamento entre histogramas TRIX(288) e TSI(288,144). +1=bullish, -1=bearish, 0=neutro.",
                "{-1, 0, +1}",
                "TRIX_hist<0 -> -1, TSI_hist<0 -> -1; resultado=+1"
        );
    }
}
