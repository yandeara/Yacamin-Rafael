package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleLambdaExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleAccW4W16Calc implements DescribableCalc {

    public static double calculate(KyleLambdaExtension lambda, int index) {
        double current = lambda.getValue(index).doubleValue();
        double lagged = lambda.getValue(index - 4).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w16_acc_w4",
                "Kyle Lambda W16 Acceleration W4",
                "microstructure",
                "(lambda[t] - lambda[t-4]) / max(abs(lambda[t-4]), 1e-12)",
                "Aceleracao percentual do Kyle Lambda W16 em 4 barras. " +
                "Detecta mudancas rapidas no impacto estrutural de preco, sinalizando transicoes abruptas de profundidade de mercado.",
                "unbounded",
                "lambda[t]=0.00015, lambda[t-4]=0.00012 -> (0.00015-0.00012)/0.00012 = 0.25"
        );
    }
}
