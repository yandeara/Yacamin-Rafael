package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;

import org.springframework.stereotype.Component;

@Component
public class MomClose14SlpCalc implements DescribableCalc {

    public static double calculate(LinearRegressionSlopeIndicator slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_close_14_slp",
                "Close Slope 14",
                "momentum",
                "LinearRegressionSlope(close, 14)",
                "Inclinacao da regressao linear do preco de fechamento na janela de 14 periodos.",
                "R",
                "Slope positivo indica tendencia de alta no periodo."
        );
    }
}
