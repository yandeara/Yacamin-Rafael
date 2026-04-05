package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension.KyleSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class KyleSlpW20W16Calc implements DescribableCalc {

    public static double calculate(KyleSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_kyle_lambda_w16_slp_w20",
                "Kyle Lambda W16 Slope W20",
                "microstructure",
                "linreg_slope(kyle_lambda_w16, 20)",
                "Inclinacao da regressao linear do Kyle Lambda W16 nas ultimas 20 barras. " +
                "Mede a tendencia de medio prazo do impacto estrutural de preco, revelando deterioracao ou melhora gradual da profundidade.",
                "unbounded",
                "lambda com tendencia de queda em 20 barras -> slope ~ -0.000003"
        );
    }
}
