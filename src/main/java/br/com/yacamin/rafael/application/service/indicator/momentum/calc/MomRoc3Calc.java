package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ROCIndicator;

@Component
public class MomRoc3Calc implements DescribableCalc {

    public static double calculate(ROCIndicator roc, int index) {
        return roc.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_roc_3",
                "ROC(3)",
                "momentum",
                "ROC(3)",
                "Rate of Change com periodo 3. Mede a variacao percentual do preco em relacao a 3 barras atras.",
                "unbounded (tipicamente -10 a 10)",
                "Preco subiu 2% em 3 barras -> ROC(3) = 2.0"
        );
    }
}
