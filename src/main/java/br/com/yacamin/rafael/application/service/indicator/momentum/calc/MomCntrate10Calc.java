package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.ContinuationRateExtension;

import org.springframework.stereotype.Component;

@Component
public class MomCntrate10Calc implements DescribableCalc {

    public static double calculate(ContinuationRateExtension continuationRate, int index) {
        return continuationRate.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_cntrate_10",
                "Continuation Rate 10",
                "momentum",
                "directional persistence of ret_1 over 10 bars",
                "Taxa de continuacao direcional nos ultimos 10 periodos. " +
                "Mede a persistencia do sinal do retorno (quantas vezes ret_1 manteve a mesma direcao).",
                "[0, 1]",
                "8 de 10 barras com mesmo sinal -> 0.8"
        );
    }
}
