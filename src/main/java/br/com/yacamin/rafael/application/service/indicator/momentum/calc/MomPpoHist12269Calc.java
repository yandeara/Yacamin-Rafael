package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Component
public class MomPpoHist12269Calc implements DescribableCalc {

    public static double calculate(PPOIndicator ppo, EMAIndicator signal, int index) {
        return ppo.getValue(index).doubleValue() - signal.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_ppo_hist_12_26_9",
                "PPO Histogram 12/26/9",
                "momentum",
                "PPO(12,26) - Signal(9)",
                "Histograma do PPO (diferenca entre PPO e sinal). Positivo indica momentum bullish acelerando, negativo bearish.",
                "unbounded (tipicamente -1 a 1)",
                "PPO=0.8, Signal=0.5 -> 0.3"
        );
    }
}
