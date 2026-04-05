package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension.AmihudSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class AmihudSlpW20Calc implements DescribableCalc {

    public static double calculate(AmihudSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_amihud_slp_w20",
                "Amihud Slope W20",
                "microstructure",
                "linreg_slope(amihud, 20)",
                "Inclinacao da regressao linear do Amihud nas ultimas 20 barras. " +
                "Captura a tendencia de medio prazo da iliquidez, util para identificar deterioracao ou melhora gradual de liquidez.",
                "unbounded",
                "amihud estavel em 20 barras -> slope ~ 0.0"
        );
    }
}
