package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.WickImbalanceExtension;

import org.springframework.stereotype.Component;

@Component
public class WickImbalanceCalc implements DescribableCalc {

    public static double calculate(WickImbalanceExtension imb, int index) {
        return imb.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_wick_imbalance",
                "Wick Imbalance",
                "microstructure",
                "(upper - lower) / (upper + lower)",
                "Desequilibrio entre pavios superior e inferior. " +
                "+1 = so pavio superior, -1 = so pavio inferior, 0 = equilibrado.",
                "-1 to 1",
                "upper=2.0, lower=1.0 -> (2-1)/(2+1) = 0.333"
        );
    }
}
