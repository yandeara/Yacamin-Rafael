package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.CCIIndicator;

@Component
public class MomCci14DstMidCalc implements DescribableCalc {

    public static double calculate(CCIIndicator cci, int index) {
        return Math.abs(cci.getValue(index).doubleValue());
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_cci_14_dst_mid",
                "CCI(14) Distance from 0",
                "momentum",
                "|CCI(14)|",
                "Distancia absoluta do CCI(14) em relacao ao ponto neutro 0. Valores altos indicam momentum extremo independente da direcao.",
                "0+",
                "CCI(14) = -150 -> dst_mid = 150.0"
        );
    }
}
