package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.DecayRateExtension;

import org.springframework.stereotype.Component;

@Component
public class MomDecay10Calc implements DescribableCalc {

    public static double calculate(DecayRateExtension decayRate, int index) {
        return decayRate.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_decay_10",
                "Decay Rate 10",
                "momentum",
                "slope of |ret_1| over 10 bars",
                "Taxa de decaimento da magnitude do retorno nos ultimos 10 periodos. " +
                "Valores negativos indicam que a volatilidade esta diminuindo; positivos indicam aumento.",
                "R",
                "slope of |ret_1| over 10 bars = -0.001"
        );
    }
}
