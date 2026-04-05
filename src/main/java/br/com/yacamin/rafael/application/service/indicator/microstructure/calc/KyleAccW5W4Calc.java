package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleLambdaExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleAccW5W4Calc implements DescribableCalc {

    public static double calculate(KyleLambdaExtension lambda, int index) {
        double current = lambda.getValue(index).doubleValue();
        double lagged = lambda.getValue(index - 5).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w4_acc_w5",
                "Kyle Lambda W4 Acceleration W5",
                "microstructure",
                "(lambda[t] - lambda[t-5]) / max(abs(lambda[t-5]), 1e-12)",
                "Aceleracao percentual do Kyle Lambda W4 em 5 barras. " +
                "Captura a velocidade de mudanca do impacto de preco em janela ligeiramente maior, suavizando ruido do curtissimo prazo.",
                "unbounded",
                "lambda[t]=0.00025, lambda[t-5]=0.00015 -> (0.00025-0.00015)/0.00015 = 0.67"
        );
    }
}
