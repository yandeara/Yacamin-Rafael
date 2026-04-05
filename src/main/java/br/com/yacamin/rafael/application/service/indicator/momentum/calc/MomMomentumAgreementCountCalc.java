package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomMomentumAgreementCountCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double... aligns) {
        if (aligns == null || aligns.length == 0) return 0.0;
        int c = 0;
        for (double a : aligns) {
            if (a > EPS) c++;
        }
        return (double) c;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_momentum_agreement_count",
                "Momentum Agreement Count",
                "momentum",
                "count(align == +1)",
                "Numero de alinhamentos com valor +1 (bullish) entre os 6 pares de indicadores de momentum.",
                "{0, 1, 2, 3, 4, 5, 6}",
                "5 pares bullish -> agreement_count = 5"
        );
    }
}
