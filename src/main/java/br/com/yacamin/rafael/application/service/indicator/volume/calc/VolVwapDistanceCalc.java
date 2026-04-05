package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
@Component
public class VolVwapDistanceCalc implements DescribableCalc {
    public static double calculate(BarSeries series, int index, ATRIndicator atr) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double vwap = VolVwapCalc.vwap(series, 32);
        double atrVal = atr.getValue(index).doubleValue();
        return (close - vwap) / atrVal;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_vwap_distance", "VWAP Distance", "volume", "(close-vwap)/ATR14", "Distancia do close ao VWAP normalizada por ATR.", "unbounded", ""); }
}
