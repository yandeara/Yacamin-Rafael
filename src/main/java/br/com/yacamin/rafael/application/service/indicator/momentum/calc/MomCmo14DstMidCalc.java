package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.CMOIndicator;

@Component
public class MomCmo14DstMidCalc implements DescribableCalc {

    public static double calculate(CMOIndicator cmo, int index) {
        return Math.abs(cmo.getValue(index).doubleValue());
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_cmo_14_dst_mid",
                "CMO(14) Distance from 0",
                "momentum",
                "|CMO(14)|",
                "Distancia absoluta do CMO(14) em relacao ao ponto neutro 0. Quanto maior, mais extremo o momentum.",
                "0 a 100",
                "CMO(14) = -60 -> dst_mid = 60.0"
        );
    }
}
