package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Component
public class MomPpoHist12269DltCalc implements DescribableCalc {

    public static double calculate(PPOIndicator ppo, EMAIndicator signal, int index) {
        double hNow = ppo.getValue(index).doubleValue() - signal.getValue(index).doubleValue();
        double hPrev = ppo.getValue(index - 1).doubleValue() - signal.getValue(index - 1).doubleValue();
        return hNow - hPrev;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_ppo_hist_12_26_9_dlt",
                "PPO Histogram 12/26/9 Delta",
                "momentum",
                "hist[t] - hist[t-1]",
                "Variacao do histograma PPO entre barras consecutivas. Indica se o momentum esta acelerando ou desacelerando.",
                "unbounded",
                "hist sobe de 0.1 para 0.3 -> 0.2"
        );
    }
}
