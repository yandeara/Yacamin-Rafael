package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VltVolPark32SlpCalc implements DescribableCalc {
    private static double park(BarSeries s, int last, int period) {
        int start = Math.max(0, last - period + 1);
        int n = last - start + 1;
        double sumSq = 0.0;
        for (int i = start; i <= last; i++) {
            double h = s.getBar(i).getHighPrice().doubleValue();
            double l = s.getBar(i).getLowPrice().doubleValue();
            double logHL = Math.log(h / l);
            sumSq += logHL * logHL;
        }
        return Math.sqrt(sumSq / (4.0 * n * Math.log(2.0)));
    }
    public static double calculate(BarSeries s, int idx) {
        int slopeW = 20;
        int start = Math.max(1, idx - slopeW + 1);
        int n = idx - start + 1;
        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
        int k = 0;
        for (int i = start; i <= idx; i++) {
            double x = k++;
            double y = park(s, i, 32);
            sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x;
        }
        double denom = n * sumX2 - (sumX * sumX);
        if (denom == 0.0) return 0.0;
        return (n * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_vol_park_32_slp", "PARK Vol 32 Slp", "volatility", "slope(park, 20)", "Slope PARK 32.", "unbounded", ""); }
}
