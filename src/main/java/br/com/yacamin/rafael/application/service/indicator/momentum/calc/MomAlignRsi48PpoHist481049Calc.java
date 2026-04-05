package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;

@Component
public class MomAlignRsi48PpoHist481049Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double rsi48, double ppoHist48) {
        return sign(rsi48 - 50.0) * sign(ppoHist48);
    }

    private static double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_align_rsi_48_ppo_hist_48_104_9",
                "Alignment RSI(48) vs PPO Hist(48,104,9)",
                "momentum",
                "sign(RSI(48) - 50) * sign(PPO_hist(48,104,9))",
                "Alinhamento entre RSI(48) e histograma PPO(48,104,9). +1=bullish, -1=bearish, 0=neutro.",
                "{-1, 0, +1}",
                "RSI=55 (>50 -> +1), PPO_hist<0 -> -1; resultado=-1"
        );
    }
}
