package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolBapSlope16Calc implements DescribableCalc {
    public static double calculate(BarSeries series, int index) {
        int w = 16; int n = w;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < w; i++) {
            double x = i; double y = VolBapCalc.bap(series, index - i);
            sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x;
        }
        double denom = n * sumX2 - sumX * sumX;
        return (n * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_bap_slope_16", "BAP Slope 16", "volume", "slope(bap, 16)", "Slope BAP 16.", "unbounded", ""); }
}
