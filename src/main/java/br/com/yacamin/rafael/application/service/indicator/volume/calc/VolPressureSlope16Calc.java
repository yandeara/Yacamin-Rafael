package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolPressureSlope16Calc implements DescribableCalc {
    private static double spTrades(BarSeries s, int index, int k) {
        int st = index - k + 1; int score = 0;
        for (int i = st + 1; i <= index; i++) { double d = s.getBar(i).getTrades() - s.getBar(i - 1).getTrades(); score += d > 0 ? 1 : (d < 0 ? -1 : 0); }
        return (double) score / (double) k;
    }
    public static double calculate(BarSeries series, int index) {
        int w = 16; int start = index - w + 1;
        if (start < 0) return 0.0;
        double[] sp = new double[w];
        for (int i = 0; i < w; i++) sp[i] = spTrades(series, start + i, w);
        int n = w; double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) { double x = i; sumX += x; sumY += sp[i]; sumXY += x * sp[i]; sumX2 += x * x; }
        double denom = n * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-12) return 0.0;
        return (n * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_pressure_slope_16", "Pressure Slope 16", "volume", "slope(sp_trades, 16)", "Slope pressao trades janela 16.", "unbounded", ""); }
}
