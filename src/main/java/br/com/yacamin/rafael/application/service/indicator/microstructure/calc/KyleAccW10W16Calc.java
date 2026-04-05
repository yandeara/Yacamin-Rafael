package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleLambdaExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleAccW10W16Calc implements DescribableCalc {

    public static double calculate(KyleLambdaExtension lambda, int index) {
        double current = lambda.getValue(index).doubleValue();
        double lagged = lambda.getValue(index - 10).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w16_acc_w10",
                "Kyle Lambda W16 Acceleration W10",
                "microstructure",
                "(lambda[t] - lambda[t-10]) / max(abs(lambda[t-10]), 1e-12)",
                "Aceleracao percentual do Kyle Lambda W16 em 10 barras. " +
                "Captura mudancas de medio prazo no impacto estrutural de preco, sinalizando evolucao da profundidade do mercado.",
                "unbounded",
                "lambda[t]=0.00016, lambda[t-10]=0.00012 -> (0.00016-0.00012)/0.00012 = 0.33"
        );
    }
}
