package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomAlignRsi14PpoHist12269Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double rsi14, double ppoHist12) {
        return sign(rsi14 - 50.0) * sign(ppoHist12);
    }

    private static double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_align_rsi_14_ppo_hist_12_26_9",
                "Alignment RSI(14) vs PPO Hist(12,26,9)",
                "momentum",
                "sign(RSI(14) - 50) * sign(PPO_hist(12,26,9))",
                "Alinhamento entre RSI(14) e histograma PPO(12,26,9). +1=bullish, -1=bearish, 0=neutro.",
                "{-1, 0, +1}",
                "RSI=60 (>50 -> +1), PPO_hist>0 -> +1; resultado=+1"
        );
    }
}
