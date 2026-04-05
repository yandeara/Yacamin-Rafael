package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.LowerWickExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickLowerPctCalc implements DescribableCalc {

    public static double calculate(LowerWickExtension lower, BarSeries series, int index) {
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double range = high - low;
        if (Math.abs(range) < 1e-12) return 0;
        return lower.getValue(index).doubleValue() / range;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_lower_wick_pct",
                "Lower Wick Pct",
                "microstructure",
                "lower_wick / (high - low)",
                "Percentual do range ocupado pelo pavio inferior. " +
                "Indica a proporcao de rejeicao na minima relativa ao range total.",
                "0-1",
                "lower_wick=1.0, high=102, low=99 -> 1.0/3.0 = 0.333"
        );
    }
}
