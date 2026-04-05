package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolMicroburstVolumeIntensity16Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int idx = series.getEndIndex(); int p = 16; int start = idx - p + 1;
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += series.getBar(i).getVolume().doubleValue();
        double mean = sum / p;
        return series.getBar(idx).getVolume().doubleValue() / mean;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_microburst_volume_intensity_16", "Microburst Volume Intensity 16", "volume", "vol/mean(vol)", "Intensidade volume janela 16.", "0+", ""); }
}
