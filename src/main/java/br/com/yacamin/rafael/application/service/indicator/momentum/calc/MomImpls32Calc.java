package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.ImpulseExtension;

import org.springframework.stereotype.Component;

@Component
public class MomImpls32Calc implements DescribableCalc {

    public static double calculate(ImpulseExtension impulse, int index) {
        return impulse.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_impls_32",
                "Impulse 32",
                "momentum",
                "sum(ret_1) over 32 bars (directional accumulated)",
                "Impulso direcional acumulado nos ultimos 32 periodos. " +
                "Soma dos retornos de 1 barra, capturando a forca e direcao do movimento acumulado.",
                "R",
                "sum(ret_1) over 32 bars = 0.045"
        );
    }
}
