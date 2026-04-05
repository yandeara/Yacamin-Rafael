package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleLambdaExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleAccW4W4Calc implements DescribableCalc {

    public static double calculate(KyleLambdaExtension lambda, int index) {
        double current = lambda.getValue(index).doubleValue();
        double lagged = lambda.getValue(index - 4).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w4_acc_w4",
                "Kyle Lambda W4 Acceleration W4",
                "microstructure",
                "(lambda[t] - lambda[t-4]) / max(abs(lambda[t-4]), 1e-12)",
                "Aceleracao percentual do Kyle Lambda W4 em 4 barras. " +
                "Mede a taxa de variacao do impacto de preco no curtissimo prazo, detectando choques abruptos de liquidez.",
                "unbounded",
                "lambda[t]=0.00020, lambda[t-4]=0.00010 -> (0.00020-0.00010)/0.00010 = 1.0"
        );
    }
}
