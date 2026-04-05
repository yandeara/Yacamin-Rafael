package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleSlpW20W4Calc implements DescribableCalc {

    public static double calculate(KyleSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w4_slp_w20",
                "Kyle Lambda W4 Slope W20",
                "microstructure",
                "linreg_slope(kyle_lambda_w4, 20)",
                "Inclinacao da regressao linear do Kyle Lambda W4 nas ultimas 20 barras. " +
                "Captura a tendencia de medio prazo do impacto de preco, indicando se a liquidez esta melhorando ou piorando gradualmente.",
                "unbounded",
                "lambda com tendencia de alta em 20 barras -> slope ~ 0.000005"
        );
    }
}
