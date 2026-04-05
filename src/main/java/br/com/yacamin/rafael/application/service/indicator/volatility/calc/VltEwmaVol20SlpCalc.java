package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VltEwmaVol20SlpCalc implements DescribableCalc {
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
        int sW = 20; int start = Math.max(1, index - sW + 1); int n = index - start + 1;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0; int k = 0;
        for (int i = start; i <= index; i++) { double x = k++; double y = ewmaVol(series, i, 20); sumX += x; sumY += y; sumXY += x*y; sumX2 += x*x; }
        double denom = n * sumX2 - sumX * sumX;
        return (Math.abs(denom) < 1e-12) ? 0 : (n * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_ewma_vol_20_slp", "EWMA Vol 20 Slope", "volatility", "slope(ewma, 20)", "Slope EWMA vol.", "unbounded", ""); }
}
