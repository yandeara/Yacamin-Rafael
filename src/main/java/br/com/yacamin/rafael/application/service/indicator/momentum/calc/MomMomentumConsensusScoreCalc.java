package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomMomentumConsensusScoreCalc implements DescribableCalc {

    public static double calculate(double a1, double a2, double a3, double a4, double a5, double a6) {
        return (a1 + a2 + a3 + a4 + a5 + a6) / 6.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_momentum_consensus_score",
                "Momentum Consensus Score",
                "momentum",
                "mean(align_rsi14_ppo12, align_rsi48_ppo48, align_rsi288_ppo288, align_trix9_tsi25, align_trix48_tsi48, align_trix288_tsi288)",
                "Media dos 6 alinhamentos de momentum. Indica consenso geral entre indicadores.",
                "[-1, +1]",
                "Todos +1 -> consensus=1.0; metade +1 metade -1 -> consensus=0.0"
        );
    }
}
