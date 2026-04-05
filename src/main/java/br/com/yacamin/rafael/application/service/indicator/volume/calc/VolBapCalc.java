package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolBapCalc implements DescribableCalc {
    static double bap(BarSeries s, int i) {
        return (s.getBar(i).getClosePrice().doubleValue() - s.getBar(i).getOpenPrice().doubleValue()) / s.getBar(i).getVolume().doubleValue();
    }
    public static double calculate(BarSeries series, int index) { return bap(series, index); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_bap", "BAP", "volume", "(close-open)/volume", "Bid-ask pressure.", "unbounded", ""); }
}
