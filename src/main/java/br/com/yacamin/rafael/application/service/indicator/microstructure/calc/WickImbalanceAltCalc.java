package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.WickImbalanceExtension;

import org.springframework.stereotype.Component;

@Component
public class WickImbalanceAltCalc implements DescribableCalc {

    public static double calculate(WickImbalanceExtension imb, int index) {
        return WickImbalanceCalc.calculate(imb, index);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_wick_imbalance",
                "Wick Imbalance Alt",
                "microstructure",
                "(upper - lower) / (upper + lower)",
                "Alias de wick_imbalance. Desequilibrio normalizado entre pavios. " +
                "Positivo indica dominancia do pavio superior, negativo do inferior.",
                "-1 to 1",
                "upper=2.0, lower=1.0 -> (2-1)/(2+1) = 0.333"
        );
    }
}
