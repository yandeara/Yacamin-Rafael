package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolMicroburstSlope16Calc implements DescribableCalc {
    private static double intVol(BarSeries s, int idx, int p) {
        int st = idx - p + 1; double sum = 0;
        for (int i = st; i <= idx; i++) sum += s.getBar(i).getVolume().doubleValue();
        return s.getBar(idx).getVolume().doubleValue() / (sum / p);
    }
    private static double intTrades(BarSeries s, int idx, int p) {
        int st = idx - p + 1; double sum = 0;
        for (int i = st; i <= idx; i++) sum += s.getBar(i).getTrades();
        return s.getBar(idx).getTrades() / (sum / p);
    }
    public static double calculate(BarSeries series, int index) {
        int w = 16; int start = index - w + 1;
        if (start < 0) return 0.0;
        double[] mb = new double[w];
        for (int i = 0; i < w; i++) { int idx = start + i; mb[i] = (intVol(series, idx, w) - 1.0) + (intTrades(series, idx, w) - 1.0); }
        int n = w; double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) { double x = i; sumX += x; sumY += mb[i]; sumXY += x * mb[i]; sumX2 += x * x; }
        double denom = n * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-12) return 0.0;
        return (n * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_microburst_slope_16", "Microburst Slope 16", "volume", "slope(intensity_combo, 16)", "Slope microburst janela 16.", "unbounded", ""); }
}
