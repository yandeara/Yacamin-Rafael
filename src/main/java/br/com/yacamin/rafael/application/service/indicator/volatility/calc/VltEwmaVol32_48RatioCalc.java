package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VltEwmaVol32_48RatioCalc implements DescribableCalc {
    private static double ewmaVol(BarSeries s, int last, int w) {
        double lambda = 0.97; int start = Math.max(1, last - w + 1);
        double sumW = 0, sumWR2 = 0; int k = 0;
        for (int i = last; i >= start; i--) {
            double c = s.getBar(i).getClosePrice().doubleValue();
            double p = s.getBar(i-1).getClosePrice().doubleValue();
            double r = Math.log(c / p); double wt = Math.pow(lambda, k++);
            sumW += wt; sumWR2 += wt * r * r;
        }
        return Math.sqrt(sumWR2 / sumW);
    }
    public static double calculate(BarSeries series, int index) {
        double s = ewmaVol(series, index, 32); double l = ewmaVol(series, index, 48);
        return (Math.abs(l) < 1e-12) ? 0 : s / l;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_ewma_vol_32_48_ratio", "EWMA 32/48 Ratio", "volatility", "ewma32/ewma48", "Razao EWMA vol.", "0+", ""); }
}
