package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolVolumeDelta1Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int end = series.getEndIndex();
        double now = series.getBar(end).getVolume().doubleValue();
        double prev = series.getBar(end - 1).getVolume().doubleValue();
        return (now - prev) / Math.abs(prev);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_volume_delta_1", "Volume Delta 1", "volume", "(vol[t]-vol[t-1])/|vol[t-1]|", "Delta percentual do volume lag 1.", "unbounded", "vol=1100 prev=1000 -> 0.1"); }
}
