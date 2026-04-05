package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension.AmihudSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class AmihudSlpW4Calc implements DescribableCalc {

    public static double calculate(AmihudSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_amihud_slp_w4",
                "Amihud Slope W4",
                "microstructure",
                "linreg_slope(amihud, 4)",
                "Inclinacao da regressao linear do Amihud nas ultimas 4 barras. " +
                "Slope positivo indica iliquidez crescente rapida, negativo indica melhora de liquidez no curtissimo prazo.",
                "unbounded",
                "amihud subindo linearmente em 4 barras de 0.00001 a 0.00004 -> slope ~ 0.00001"
        );
    }
}
