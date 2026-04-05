package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.PPOIndicator;

@Component
public class MomPpo1226DltCalc implements DescribableCalc {

    public static double calculate(PPOIndicator ppo, int index) {
        return ppo.getValue(index).doubleValue() - ppo.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_ppo_12_26_dlt",
                "PPO 12/26 Delta",
                "momentum",
                "PPO(12,26)[t] - PPO(12,26)[t-1]",
                "Variacao do PPO 12/26 entre barras consecutivas. Indica aceleracao ou desaceleracao do momentum.",
                "unbounded",
                "PPO sobe de 0.3 para 0.5 -> 0.2"
        );
    }
}
