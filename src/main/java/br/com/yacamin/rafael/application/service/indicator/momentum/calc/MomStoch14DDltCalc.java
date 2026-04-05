package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;

@Component
public class MomStoch14DDltCalc implements DescribableCalc {

    public static double calculate(StochasticOscillatorDIndicator d, int index) {
        return d.getValue(index).doubleValue() - d.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_stoch_14_d_dlt",
                "Stochastic %D 14 Delta",
                "momentum",
                "%D(14)[t] - %D(14)[t-1]",
                "Variacao de %D(14) entre barras consecutivas. Indica a direcao da media movel do oscilador.",
                "unbounded (tipicamente -15 a 15)",
                "%D sobe de 40 para 43 -> 3.0"
        );
    }
}
