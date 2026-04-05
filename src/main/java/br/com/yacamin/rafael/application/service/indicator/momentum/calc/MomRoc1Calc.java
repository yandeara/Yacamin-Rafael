package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ROCIndicator;

@Component
public class MomRoc1Calc implements DescribableCalc {

    public static double calculate(ROCIndicator roc, int index) {
        return roc.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_roc_1",
                "ROC(1)",
                "momentum",
                "ROC(1)",
                "Rate of Change com periodo 1. Mede a variacao percentual do preco em relacao a 1 barra atras.",
                "unbounded (tipicamente -10 a 10)",
                "Preco subiu 2% em 1 barra -> ROC(1) = 2.0"
        );
    }
}
