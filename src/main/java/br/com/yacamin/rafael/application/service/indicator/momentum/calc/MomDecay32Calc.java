package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.DecayRateExtension;

import org.springframework.stereotype.Component;

@Component
public class MomDecay32Calc implements DescribableCalc {

    public static double calculate(DecayRateExtension decayRate, int index) {
        return decayRate.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_decay_32",
                "Decay Rate 32",
                "momentum",
                "slope of |ret_1| over 32 bars",
                "Taxa de decaimento da magnitude do retorno nos ultimos 32 periodos. " +
                "Valores negativos indicam que a volatilidade esta diminuindo; positivos indicam aumento.",
                "R",
                "slope of |ret_1| over 32 bars = -0.001"
        );
    }
}
