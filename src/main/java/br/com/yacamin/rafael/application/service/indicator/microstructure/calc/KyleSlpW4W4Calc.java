package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleSlpW4W4Calc implements DescribableCalc {

    public static double calculate(KyleSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w4_slp_w4",
                "Kyle Lambda W4 Slope W4",
                "microstructure",
                "linreg_slope(kyle_lambda_w4, 4)",
                "Inclinacao da regressao linear do Kyle Lambda W4 nas ultimas 4 barras. " +
                "Slope positivo indica impacto de preco crescente rapido, sinalizando deterioracao abrupta de liquidez.",
                "unbounded",
                "lambda subindo linearmente em 4 barras -> slope ~ 0.00002"
        );
    }
}
