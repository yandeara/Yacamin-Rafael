package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.CCIIndicator;

@Component
public class MomCci20DstMidCalc implements DescribableCalc {

    public static double calculate(CCIIndicator cci, int index) {
        return Math.abs(cci.getValue(index).doubleValue());
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_cci_20_dst_mid",
                "CCI(20) Distance from 0",
                "momentum",
                "|CCI(20)|",
                "Distancia absoluta do CCI(20) em relacao ao ponto neutro 0.",
                "0+",
                "CCI(20) = 120 -> dst_mid = 120.0"
        );
    }
}
