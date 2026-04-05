package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolVolumeDelta3Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int end = series.getEndIndex();
        double now = series.getBar(end).getVolume().doubleValue();
        double prev = series.getBar(end - 3).getVolume().doubleValue();
        return (now - prev) / Math.abs(prev);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_volume_delta_3", "Volume Delta 3", "volume", "(vol[t]-vol[t-3])/|vol[t-3]|", "Delta percentual do volume lag 3.", "unbounded", ""); }
}
