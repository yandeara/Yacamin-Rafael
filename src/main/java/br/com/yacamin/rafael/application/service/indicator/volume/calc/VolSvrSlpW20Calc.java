package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolSvrSlpW20Calc implements DescribableCalc {
    public static double calculate(BarSeries series, int index) {
        int w = 20; int start = index - w + 1;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0; int k = 0;
        for (int i = start; i <= index; i++) {
            double x = k++; double y = VolSvrCalc.svr(series, i);
            sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x;
        }
        double denom = w * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-12) return 0.0;
        return (w * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_svr_slp_w20", "SVR Slope W20", "volume", "slope(svr, 20)", "Slope SVR 20.", "unbounded", ""); }
}
