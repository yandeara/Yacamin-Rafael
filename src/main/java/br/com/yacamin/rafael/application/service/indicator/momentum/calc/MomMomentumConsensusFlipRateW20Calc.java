package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomMomentumConsensusFlipRateW20Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double[] consensusWindow) {
        if (consensusWindow == null || consensusWindow.length < 2) return 0.0;
        int flips = 0;
        double prevSign = 0.0;
        for (double v : consensusWindow) {
            double s = sign(v);
            if (s != 0.0) { prevSign = s; break; }
        }
        if (prevSign == 0.0) return 0.0;
        for (int i = 1; i < consensusWindow.length; i++) {
            double s = sign(consensusWindow[i]);
            if (s != 0.0 && prevSign != 0.0 && s != prevSign) flips++;
            if (s != 0.0) prevSign = s;
        }
        return flips / (double) (consensusWindow.length - 1);
    }

    private static double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_momentum_consensus_flip_rate_w20",
                "Momentum Consensus Flip Rate W20",
                "momentum",
                "flips(sign(consensus), 20) / 19",
                "Taxa de inversao de sinal do score de consenso na janela de 20 periodos. Alto=mercado indeciso.",
                "[0, 1]",
                "7 flips em 20 barras -> flip_rate = 7/19 ~ 0.37"
        );
    }
}
