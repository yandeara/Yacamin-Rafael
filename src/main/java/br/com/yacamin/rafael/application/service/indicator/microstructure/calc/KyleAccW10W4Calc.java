package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleLambdaExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleAccW10W4Calc implements DescribableCalc {

    public static double calculate(KyleLambdaExtension lambda, int index) {
        double current = lambda.getValue(index).doubleValue();
        double lagged = lambda.getValue(index - 10).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w4_acc_w10",
                "Kyle Lambda W4 Acceleration W10",
                "microstructure",
                "(lambda[t] - lambda[t-10]) / max(abs(lambda[t-10]), 1e-12)",
                "Aceleracao percentual do Kyle Lambda W4 em 10 barras. " +
                "Mede a variacao do impacto de preco em janela de medio prazo, identificando tendencias de deterioracao ou melhora de liquidez.",
                "unbounded",
                "lambda[t]=0.00030, lambda[t-10]=0.00010 -> (0.00030-0.00010)/0.00010 = 2.0"
        );
    }
}
