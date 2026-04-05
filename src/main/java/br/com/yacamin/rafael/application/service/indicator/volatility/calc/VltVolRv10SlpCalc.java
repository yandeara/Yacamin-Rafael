package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.RealizedVolExtension;
import org.springframework.stereotype.Component;
@Component
public class VltVolRv10SlpCalc implements DescribableCalc {
    public static double calculate(RealizedVolExtension rv, int index) {
        int w = 20;
        int start = Math.max(0, index - w + 1);
        int n = index - start + 1;
        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
        int k = 0;
        for (int i = start; i <= index; i++) {
            double x = k++;
            double y = rv.getValue(i).doubleValue();
            sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x;
        }
        double denom = n * sumX2 - (sumX * sumX);
        if (denom == 0.0) return 0.0;
        return (n * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_vol_rv_10_slp", "RV 10 Slp", "volatility", "slope(rv, 20)", "Slope RV 10.", "unbounded", ""); }
}
