package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.CMOIndicator;

@Component
public class MomCmo14DltCalc implements DescribableCalc {

    public static double calculate(CMOIndicator cmo, int index) {
        return cmo.getValue(index).doubleValue() - cmo.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_cmo_14_dlt",
                "CMO(14) Delta",
                "momentum",
                "CMO(14)[t] - CMO(14)[t-1]",
                "Variacao absoluta do CMO(14) entre o candle atual e o anterior.",
                "unbounded (tipicamente -40 a 40)",
                "CMO(14) subiu de 30 para 45 -> delta = 15.0"
        );
    }
}
