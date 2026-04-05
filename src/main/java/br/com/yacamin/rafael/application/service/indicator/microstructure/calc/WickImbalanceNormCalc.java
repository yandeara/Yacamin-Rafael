package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.UpperWickExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.LowerWickExtension;

import org.springframework.stereotype.Component;

@Component
public class WickImbalanceNormCalc implements DescribableCalc {

    public static double calculate(UpperWickExtension upper, LowerWickExtension lower, int index) {
        double u = upper.getValue(index).doubleValue();
        double l = lower.getValue(index).doubleValue();
        double denom = u + l;
        if (denom < 1e-12) return 0;
        return (u - l) / denom;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_wick_imbalance_norm",
                "Wick Imbalance Norm",
                "microstructure",
                "(upper - lower) / (upper + lower)",
                "Desequilibrio normalizado entre pavios. " +
                "Mesmo calculo do wick_imbalance, com chave alternativa para compatibilidade.",
                "-1 to 1",
                "upper=2.0, lower=1.0 -> (2-1)/(2+1) = 0.333"
        );
    }
}
