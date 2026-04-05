package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomMomentumNeutralCountCalc implements DescribableCalc {

    public static double calculate(double agreementCount, double disagreementCount) {
        return 6.0 - agreementCount - disagreementCount;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_momentum_neutral_count",
                "Momentum Neutral Count",
                "momentum",
                "6 - agreement_count - disagreement_count",
                "Numero de alinhamentos neutros (valor 0) entre os 6 pares de indicadores de momentum.",
                "{0, 1, 2, 3, 4, 5, 6}",
                "3 agree, 2 disagree -> neutral = 1"
        );
    }
}
