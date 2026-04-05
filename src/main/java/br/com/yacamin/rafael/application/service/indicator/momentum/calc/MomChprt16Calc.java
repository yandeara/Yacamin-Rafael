package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.ChopRatioExtension;

import org.springframework.stereotype.Component;

@Component
public class MomChprt16Calc implements DescribableCalc {

    public static double calculate(ChopRatioExtension chopRatio, int index) {
        return chopRatio.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_chprt_16",
                "Chop Ratio 16",
                "momentum",
                "sign change frequency of ret_1 over 16 bars",
                "Razao de choppiness nos ultimos 16 periodos. " +
                "Frequencia de troca de sinal do retorno. Valores altos indicam mercado lateral/chop.",
                "[0, 1]",
                "10 sign changes in 16 bars -> 0.625"
        );
    }
}
