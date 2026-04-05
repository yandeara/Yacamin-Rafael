package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomAlignRsi288PpoHist2885769Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double rsi288, double ppoHist288) {
        return sign(rsi288 - 50.0) * sign(ppoHist288);
    }

    private static double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_align_rsi_288_ppo_hist_288_576_9",
                "Alignment RSI(288) vs PPO Hist(288,576,9)",
                "momentum",
                "sign(RSI(288) - 50) * sign(PPO_hist(288,576,9))",
                "Alinhamento entre RSI(288) e histograma PPO(288,576,9). +1=bullish, -1=bearish, 0=neutro.",
                "{-1, 0, +1}",
                "RSI=45 (<50 -> -1), PPO_hist<0 -> -1; resultado=+1"
        );
    }
}
