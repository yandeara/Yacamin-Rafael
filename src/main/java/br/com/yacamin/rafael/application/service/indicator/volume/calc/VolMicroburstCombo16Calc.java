package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolMicroburstCombo16Calc implements DescribableCalc {
    private static double spike(BarSeries s, int p, boolean useTrades) {
        int idx = s.getEndIndex(); int start = idx - p + 1;
        double sum = 0, sumSq = 0;
        for (int i = start; i <= idx; i++) { double v = useTrades ? s.getBar(i).getTrades() : s.getBar(i).getVolume().doubleValue(); sum += v; sumSq += v * v; }
        double mean = sum / p; double var = (sumSq / p) - (mean * mean);
        if (var < 0 && var > -1e-9) var = 0.0;
        double std = Math.sqrt(var);
        double now = useTrades ? s.getBar(idx).getTrades() : s.getBar(idx).getVolume().doubleValue();
        return (now - mean) / std;
    }
    private static double intensity(BarSeries s, int p, boolean useTrades) {
        int idx = s.getEndIndex(); int start = idx - p + 1;
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += useTrades ? s.getBar(i).getTrades() : s.getBar(i).getVolume().doubleValue();
        double mean = sum / p;
        double now = useTrades ? s.getBar(idx).getTrades() : s.getBar(idx).getVolume().doubleValue();
        return now / mean;
    }
    public static double calculate(BarSeries series) {
        int p = 16;
        double sv = spike(series, p, false);
        double st = spike(series, p, true);
        double iv = intensity(series, p, false) - 1.0;
        double it = intensity(series, p, true) - 1.0;
        return sv + st + iv * 0.5 + it * 0.5;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_microburst_combo_16", "Microburst Combo 16", "volume", "sv+st+iv*0.5+it*0.5", "Combo microburst janela 16.", "unbounded", ""); }
}
