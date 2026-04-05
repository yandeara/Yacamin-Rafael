package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.WilliamsRIndicator;

@Component
public class MomWpr28DstMidCalc implements DescribableCalc {

    private static final double MID = -50.0;

    public static double calculate(WilliamsRIndicator wpr, int index) {
        return Math.abs(wpr.getValue(index).doubleValue() - MID);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_wpr_28_dst_mid",
                "WPR(28) Distance from -50",
                "momentum",
                "|WPR(28) - (-50)|",
                "Distancia absoluta do WPR(28) em relacao ao ponto neutro -50. Quanto maior, mais extrema a posicao.",
                "0 a 50",
                "WPR(28) = -20 -> dst_mid = 30.0"
        );
    }
}
