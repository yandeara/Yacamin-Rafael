package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleLambdaExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleAccW16W4Calc implements DescribableCalc {

    public static double calculate(KyleLambdaExtension lambda, int index) {
        double current = lambda.getValue(index).doubleValue();
        double lagged = lambda.getValue(index - 16).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w4_acc_w16",
                "Kyle Lambda W4 Acceleration W16",
                "microstructure",
                "(lambda[t] - lambda[t-16]) / max(abs(lambda[t-16]), 1e-12)",
                "Aceleracao percentual do Kyle Lambda W4 em 16 barras. " +
                "Mede variacao do impacto de preco em janela mais ampla, detectando mudancas estruturais de liquidez.",
                "unbounded",
                "lambda[t]=0.00025, lambda[t-16]=0.00015 -> (0.00025-0.00015)/0.00015 = 0.67"
        );
    }
}
