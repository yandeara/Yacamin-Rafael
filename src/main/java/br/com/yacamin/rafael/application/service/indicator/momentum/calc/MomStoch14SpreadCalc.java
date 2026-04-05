package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

@Component
public class MomStoch14SpreadCalc implements DescribableCalc {

    public static double calculate(StochasticOscillatorKIndicator k, StochasticOscillatorDIndicator d, int index) {
        return k.getValue(index).doubleValue() - d.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_stoch_14_spread",
                "Stochastic 14 Spread K-D",
                "momentum",
                "%K(14) - %D(14)",
                "Diferenca entre %K e %D do Stochastic 14. Spread positivo indica %K acima de %D (momentum bullish).",
                "unbounded (tipicamente -30 a 30)",
                "%K=70, %D=60 -> 10.0"
        );
    }
}
