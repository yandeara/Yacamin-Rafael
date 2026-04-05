package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

@Component
public class MomStoch14KDltCalc implements DescribableCalc {

    public static double calculate(StochasticOscillatorKIndicator k, int index) {
        return k.getValue(index).doubleValue() - k.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_stoch_14_k_dlt",
                "Stochastic %K 14 Delta",
                "momentum",
                "%K(14)[t] - %K(14)[t-1]",
                "Variacao de %K(14) entre barras consecutivas. Positivo indica aceleracao bullish, negativo bearish.",
                "unbounded (tipicamente -20 a 20)",
                "%K sobe de 30 para 35 -> 5.0"
        );
    }
}
