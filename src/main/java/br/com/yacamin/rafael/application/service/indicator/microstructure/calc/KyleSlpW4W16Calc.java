package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleSlpW4W16Calc implements DescribableCalc {

    public static double calculate(KyleSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w16_slp_w4",
                "Kyle Lambda W16 Slope W4",
                "microstructure",
                "linreg_slope(kyle_lambda_w16, 4)",
                "Inclinacao da regressao linear do Kyle Lambda W16 nas ultimas 4 barras. " +
                "Detecta mudancas rapidas no impacto estrutural de preco, sinalizando transicoes abruptas de regime de liquidez.",
                "unbounded",
                "lambda subindo linearmente em 4 barras -> slope ~ 0.00001"
        );
    }
}
