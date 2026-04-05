package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomMomentumConsensusAbsCalc implements DescribableCalc {

    public static double calculate(double consensus) {
        return Math.abs(consensus);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_momentum_consensus_abs",
                "Momentum Consensus Absolute",
                "momentum",
                "|consensus|",
                "Valor absoluto do score de consenso. Mede a forca do consenso independente da direcao.",
                "[0, 1]",
                "consensus=-0.8 -> abs=0.8 (forte consenso bearish)"
        );
    }
}
