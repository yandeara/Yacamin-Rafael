package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolTakerBuyRatioSlpW20Calc implements DescribableCalc {
    public static double calculate(BarSeries s, int idx) {
        int w = 20; int start = Math.max(1, idx - w + 1); int n = idx - start + 1;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0; int k = 0;
        for (int i = start; i <= idx; i++) { double x = k++; double y = VolTakerBuyRatioCalc.tbr(s, i); sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x; }
        double denom = n * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-12) return 0.0;
        return (n * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_taker_buy_ratio_slp_w20", "TBR Slope W20", "volume", "slope(tbr, 20)", "Slope TBR 20.", "unbounded", ""); }
}
