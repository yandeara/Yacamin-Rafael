package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.WickImbalanceExtension;

import org.springframework.stereotype.Component;

@Component
public class WickPressureScoreCalc implements DescribableCalc {

    public static double calculate(WickImbalanceExtension imb, int index) {
        return WickImbalanceCalc.calculate(imb, index);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_wick_pressure_score",
                "Wick Pressure Score",
                "microstructure",
                "(upper - lower) / (upper + lower)",
                "Alias de wick_imbalance como score de pressao. " +
                "Valores positivos indicam pressao vendedora, negativos pressao compradora.",
                "-1 to 1",
                "upper=2.0, lower=1.0 -> (2-1)/(2+1) = 0.333"
        );
    }
}
