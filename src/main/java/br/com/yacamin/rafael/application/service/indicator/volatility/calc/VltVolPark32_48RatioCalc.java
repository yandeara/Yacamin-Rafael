package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VltVolPark32_48RatioCalc implements DescribableCalc {
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
    public static double calculate(BarSeries s, int idx) { return park(s, idx, 32) / park(s, idx, 48); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_vol_park_32_48_ratio", "PARK 32/48 Ratio", "volatility", "park32/park48", "Razao PARK 32 vs 48.", "0+", ""); }
}
