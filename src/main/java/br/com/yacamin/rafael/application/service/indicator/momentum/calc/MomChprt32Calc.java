package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.ChopRatioExtension;

import org.springframework.stereotype.Component;

@Component
public class MomChprt32Calc implements DescribableCalc {

    public static double calculate(ChopRatioExtension chopRatio, int index) {
        return chopRatio.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_chprt_32",
                "Chop Ratio 32",
                "momentum",
                "sign change frequency of ret_1 over 32 bars",
                "Razao de choppiness nos ultimos 32 periodos. " +
                "Frequencia de troca de sinal do retorno. Valores altos indicam mercado lateral/chop.",
                "[0, 1]",
                "20 sign changes in 32 bars -> 0.625"
        );
    }
}
