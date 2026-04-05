package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TsiExtension;
import org.springframework.stereotype.Component;

@Component
public class MomTsi2513DltCalc implements DescribableCalc {

    public static double calculate(TsiExtension tsi, int index) {
        return tsi.getValue(index).doubleValue() - tsi.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_tsi_25_13_dlt",
                "TSI 25/13 Delta",
                "momentum",
                "TSI[t] - TSI[t-1]",
                "Variacao do TSI 25/13 entre barras consecutivas. Indica aceleracao ou desaceleracao do momentum.",
                "unbounded",
                "TSI sobe de 10 para 15 -> 5.0"
        );
    }
}
