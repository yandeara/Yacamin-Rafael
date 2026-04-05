package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.UpperWickExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.LowerWickExtension;

import org.springframework.stereotype.Component;

@Component
public class WickDominanceCalc implements DescribableCalc {

    public static double calculate(UpperWickExtension upper, LowerWickExtension lower, int index) {
        double u = upper.getValue(index).doubleValue();
        double l = lower.getValue(index).doubleValue();
        double denom = u + l;
        if (denom < 1e-12) return 0;
        return Math.max(u, l) / denom;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_wick_dominance",
                "Wick Dominance",
                "microstructure",
                "max(upper, lower) / (upper + lower)",
                "Grau de dominancia do maior pavio sobre o total. " +
                "0.5 = pavios iguais, 1.0 = apenas um pavio presente.",
                "0.5-1",
                "upper=3.0, lower=1.0 -> 3.0/(3.0+1.0) = 0.75"
        );
    }
}
