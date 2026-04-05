package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolBapAcc16Calc implements DescribableCalc {
    private static double slope(BarSeries s, int idx, int w) {
        int n = w; double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < w; i++) { double x = i; double y = VolBapCalc.bap(s, idx - i); sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x; }
        double denom = n * sumX2 - sumX * sumX; return (n * sumXY - sumX * sumY) / denom;
    }
    public static double calculate(BarSeries series, int index) {
        return slope(series, index, 16) - slope(series, index - 1, 16);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_bap_acc_16", "BAP Acc 16", "volume", "slope(t)-slope(t-1)", "Aceleracao BAP 16.", "unbounded", ""); }
}
