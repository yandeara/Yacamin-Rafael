package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleLambdaExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleAccW5W16Calc implements DescribableCalc {

    public static double calculate(KyleLambdaExtension lambda, int index) {
        double current = lambda.getValue(index).doubleValue();
        double lagged = lambda.getValue(index - 5).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w16_acc_w5",
                "Kyle Lambda W16 Acceleration W5",
                "microstructure",
                "(lambda[t] - lambda[t-5]) / max(abs(lambda[t-5]), 1e-12)",
                "Aceleracao percentual do Kyle Lambda W16 em 5 barras. " +
                "Mede a taxa de variacao do impacto estrutural de preco, filtrando ruido de alta frequencia.",
                "unbounded",
                "lambda[t]=0.00014, lambda[t-5]=0.00010 -> (0.00014-0.00010)/0.00010 = 0.40"
        );
    }
}
