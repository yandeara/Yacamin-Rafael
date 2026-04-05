package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomTsiHist2513SlpW20Calc implements DescribableCalc {

    public static double calculate(double[] histWindow) {
        return slope(histWindow);
    }

    private static double slope(double[] vals) {
        if (vals == null || vals.length < 2) return 0.0;
        int n = vals.length;
        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += vals[i];
            sumXY += i * vals[i];
            sumX2 += (double) i * i;
        }
        double denom = n * sumX2 - (sumX * sumX);
        if (Math.abs(denom) < 1e-12) return 0.0;
        return (n * sumXY - sumX * sumY) / denom;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_tsi_hist_25_13_slp_w20",
                "TSI Hist(25,13) Slope W20",
                "momentum",
                "linear_regression_slope(TSI_hist_25_13, 20)",
                "Inclinacao do histograma TSI(25,13) nos ultimos 20 periodos. Indica aceleracao do momentum TSI curto.",
                "unbounded",
                "Histograma subindo -> slope positivo"
        );
    }
}
