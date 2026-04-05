package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ROCIndicator;

@Component
public class MomRoc2Calc implements DescribableCalc {

    public static double calculate(ROCIndicator roc, int index) {
        return roc.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_roc_2",
                "ROC(2)",
                "momentum",
                "ROC(2)",
                "Rate of Change com periodo 2. Mede a variacao percentual do preco em relacao a 2 barras atras.",
                "unbounded (tipicamente -10 a 10)",
                "Preco subiu 2% em 2 barras -> ROC(2) = 2.0"
        );
    }
}
