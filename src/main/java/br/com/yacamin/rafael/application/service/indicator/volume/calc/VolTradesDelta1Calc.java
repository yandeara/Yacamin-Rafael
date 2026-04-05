package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolTradesDelta1Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int end = series.getEndIndex();
        double now = series.getBar(end).getTrades();
        double prev = series.getBar(end - 1).getTrades();
        return (now - prev) / Math.abs(prev);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_trades_delta_1", "Trades Delta 1", "volume", "(trades[t]-trades[t-1])/|trades[t-1]|", "Delta percentual dos trades lag 1.", "unbounded", ""); }
}
