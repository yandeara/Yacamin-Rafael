package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.UpperWickExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.LowerWickExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickTotalPctCalc implements DescribableCalc {

    public static double calculate(UpperWickExtension upper, LowerWickExtension lower, BarSeries series, int index) {
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double range = high - low;
        if (Math.abs(range) < 1e-12) return 0;
        double total = upper.getValue(index).doubleValue() + lower.getValue(index).doubleValue();
        return total / range;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_total_wick_pct",
                "Total Wick Pct",
                "microstructure",
                "(upper + lower) / range",
                "Percentual do range ocupado pela soma dos pavios. " +
                "Complemento do body_pct. Valores altos indicam candle dominado por sombras.",
                "0-1",
                "upper=1.0, lower=1.0, range=3.0 -> 2.0/3.0 = 0.667"
        );
    }
}
