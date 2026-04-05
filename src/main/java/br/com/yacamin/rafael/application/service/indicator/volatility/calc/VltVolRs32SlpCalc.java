package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VltVolRs32SlpCalc implements DescribableCalc {
    private static double rs(BarSeries s, int last, int period) {
        int start = Math.max(0, last - period + 1);
        int n = last - start + 1;
        double sum = 0.0;
        for (int i = start; i <= last; i++) {
            double o = s.getBar(i).getOpenPrice().doubleValue();
            double h = s.getBar(i).getHighPrice().doubleValue();
            double l = s.getBar(i).getLowPrice().doubleValue();
            double c = s.getBar(i).getClosePrice().doubleValue();
            double logHO = Math.log(h / o);
            double logLO = Math.log(l / o);
            double logCO = Math.log(c / o);
            sum += logHO * (logHO - logCO) + logLO * (logLO - logCO);
        }
        return Math.sqrt(sum / n);
    }
    public static double calculate(BarSeries s, int idx) {
        int slopeW = 20;
        int start = Math.max(1, idx - slopeW + 1);
        int n = idx - start + 1;
        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
        int k = 0;
        for (int i = start; i <= idx; i++) {
            double x = k++;
            double y = rs(s, i, 32);
            sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x;
        }
        double denom = n * sumX2 - (sumX * sumX);
        if (denom == 0.0) return 0.0;
        return (n * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_vol_rs_32_slp", "RS Vol 32 Slp", "volatility", "slope(rs, 20)", "Slope RS 32.", "unbounded", ""); }
}
