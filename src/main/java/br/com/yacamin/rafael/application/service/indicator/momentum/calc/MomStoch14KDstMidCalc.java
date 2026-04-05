package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

@Component
public class MomStoch14KDstMidCalc implements DescribableCalc {

    public static double calculate(StochasticOscillatorKIndicator k, int index) {
        return Math.abs(k.getValue(index).doubleValue() - 50.0);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_stoch_14_k_dst_mid",
                "Stochastic %K 14 Distance to Mid",
                "momentum",
                "|%K(14) - 50|",
                "Distancia absoluta de %K(14) ao ponto medio (50). Valores altos indicam condicoes extremas de sobrecompra/sobrevenda.",
                "0 a 50",
                "%K=80 -> |80-50| = 30.0"
        );
    }
}
