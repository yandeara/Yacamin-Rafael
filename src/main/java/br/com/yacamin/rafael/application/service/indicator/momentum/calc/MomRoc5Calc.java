package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ROCIndicator;

@Component
public class MomRoc5Calc implements DescribableCalc {

    public static double calculate(ROCIndicator roc, int index) {
        return roc.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_roc_5",
                "ROC(5)",
                "momentum",
                "ROC(5)",
                "Rate of Change com periodo 5. Mede a variacao percentual do preco em relacao a 5 barras atras.",
                "unbounded (tipicamente -10 a 10)",
                "Preco subiu 2% em 5 barras -> ROC(5) = 2.0"
        );
    }
}
