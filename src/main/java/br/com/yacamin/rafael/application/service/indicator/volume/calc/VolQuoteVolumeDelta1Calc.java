package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolQuoteVolumeDelta1Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int end = series.getEndIndex();
        double now = ((MikhaelBar) series.getBar(end)).getQuoteVolume().doubleValue();
        double prev = ((MikhaelBar) series.getBar(end - 1)).getQuoteVolume().doubleValue();
        return (now - prev) / Math.abs(prev);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_quote_volume_delta_1", "Quote Vol Delta 1", "volume", "(qvol[t]-qvol[t-1])/|qvol[t-1]|", "Delta percentual do quote volume lag 1.", "unbounded", ""); }
}
