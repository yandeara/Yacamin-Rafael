package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.UpperWickExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.LowerWickExtension;

import org.springframework.stereotype.Component;

@Component
public class WickShadowImbalanceScoreCalc implements DescribableCalc {

    public static double calculate(UpperWickExtension upper, LowerWickExtension lower, int index) {
        var bar = upper.getBarSeries().getBar(index);
        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        if (range < 1e-12) return 0;
        double u = upper.getValue(index).doubleValue();
        double l = lower.getValue(index).doubleValue();
        return (u - l) / (range + 1e-12);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_shadow_imbalance_score",
                "Shadow Imbalance Score",
                "microstructure",
                "(upper - lower) / (upper + lower)",
                "Score de desequilibrio entre sombras superior e inferior. " +
                "Equivalente ao wick imbalance, usado como score de direcao das sombras.",
                "-1 to 1",
                "upper=3.0, lower=1.0 -> (3-1)/(3+1) = 0.5"
        );
    }
}
