package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VltVolGk16_48RatioCalc implements DescribableCalc {
    private static double gk(BarSeries s, int last, int period) {
        int start = Math.max(0, last - period + 1);
        int n = last - start + 1;
        double sum = 0.0;
        double k = 2.0 * Math.log(2.0) - 1.0;
        for (int i = start; i <= last; i++) {
            double o = s.getBar(i).getOpenPrice().doubleValue();
            double h = s.getBar(i).getHighPrice().doubleValue();
            double l = s.getBar(i).getLowPrice().doubleValue();
            double c = s.getBar(i).getClosePrice().doubleValue();
            double logHL = Math.log(h / l);
            double logCO = Math.log(c / o);
            sum += 0.5 * logHL * logHL - k * logCO * logCO;
        }
        return Math.sqrt(sum / n);
    }
    public static double calculate(BarSeries s, int idx) { return gk(s, idx, 16) / gk(s, idx, 48); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_vol_gk_16_48_ratio", "GK 16/48 Ratio", "volatility", "gk16/gk48", "Razao GK 16 vs 48.", "0+", ""); }
}
