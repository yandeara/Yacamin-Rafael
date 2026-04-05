package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.UpperWickExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickUpperPctCalc implements DescribableCalc {

    public static double calculate(UpperWickExtension upper, BarSeries series, int index) {
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double range = high - low;
        if (Math.abs(range) < 1e-12) return 0;
        return upper.getValue(index).doubleValue() / range;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_upper_wick_pct",
                "Upper Wick Pct",
                "microstructure",
                "upper_wick / (high - low)",
                "Percentual do range ocupado pelo pavio superior. " +
                "Indica a proporcao de rejeicao na maxima relativa ao range total.",
                "0-1",
                "upper_wick=1.0, high=102, low=99 -> 1.0/3.0 = 0.333"
        );
    }
}
