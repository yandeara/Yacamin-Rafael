package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolVwapCalc implements DescribableCalc {
    static double vwap(BarSeries series, int lookback) {
        int end = series.getEndIndex(); int start = end - lookback + 1;
        double pvSum = 0, volSum = 0;
        for (int i = start; i <= end; i++) { double p = series.getBar(i).getClosePrice().doubleValue(); double v = series.getBar(i).getVolume().doubleValue(); pvSum += p * v; volSum += v; }
        return pvSum / volSum;
    }
    public static double calculate(BarSeries series) { return vwap(series, 32); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_vwap", "VWAP", "volume", "sum(p*v)/sum(v)", "VWAP janela 32.", "0+", ""); }
}
