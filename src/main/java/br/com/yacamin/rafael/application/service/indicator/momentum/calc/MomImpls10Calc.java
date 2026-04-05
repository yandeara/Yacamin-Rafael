package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.ImpulseExtension;

import org.springframework.stereotype.Component;

@Component
public class MomImpls10Calc implements DescribableCalc {

    public static double calculate(ImpulseExtension impulse, int index) {
        return impulse.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_impls_10",
                "Impulse 10",
                "momentum",
                "sum(ret_1) over 10 bars (directional accumulated)",
                "Impulso direcional acumulado nos ultimos 10 periodos. " +
                "Soma dos retornos de 1 barra, capturando a forca e direcao do movimento acumulado.",
                "R",
                "sum(ret_1) over 10 bars = 0.045"
        );
    }
}
