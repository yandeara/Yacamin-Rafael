package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TsiExtension;
import org.springframework.stereotype.Component;

@Component
public class MomTsi2513DstMidCalc implements DescribableCalc {

    public static double calculate(TsiExtension tsi, int index) {
        return Math.abs(tsi.getValue(index).doubleValue());
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_tsi_25_13_dst_mid",
                "TSI 25/13 Distance to Mid",
                "momentum",
                "|TSI(25,13)|",
                "Distancia absoluta do TSI ao ponto medio (0). Valores altos indicam momentum forte em qualquer direcao.",
                "0 a 100",
                "TSI=-20 -> 20.0"
        );
    }
}
