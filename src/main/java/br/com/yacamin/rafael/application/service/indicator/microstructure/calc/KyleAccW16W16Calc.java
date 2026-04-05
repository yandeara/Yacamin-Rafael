package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleLambdaExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleAccW16W16Calc implements DescribableCalc {

    public static double calculate(KyleLambdaExtension lambda, int index) {
        double current = lambda.getValue(index).doubleValue();
        double lagged = lambda.getValue(index - 16).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w16_acc_w16",
                "Kyle Lambda W16 Acceleration W16",
                "microstructure",
                "(lambda[t] - lambda[t-16]) / max(abs(lambda[t-16]), 1e-12)",
                "Aceleracao percentual do Kyle Lambda W16 em 16 barras. " +
                "Captura a evolucao do impacto estrutural de preco sobre uma janela equivalente, detectando ciclos de liquidez.",
                "unbounded",
                "lambda[t]=0.00018, lambda[t-16]=0.00012 -> (0.00018-0.00012)/0.00012 = 0.50"
        );
    }
}
