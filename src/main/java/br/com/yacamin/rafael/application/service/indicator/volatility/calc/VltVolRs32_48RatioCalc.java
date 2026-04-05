package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VltVolRs32_48RatioCalc implements DescribableCalc {
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
    public static double calculate(BarSeries s, int idx) { return rs(s, idx, 32) / rs(s, idx, 48); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_vol_rs_32_48_ratio", "RS 32/48 Ratio", "volatility", "rs32/rs48", "Razao RS 32 vs 48.", "0+", ""); }
}
