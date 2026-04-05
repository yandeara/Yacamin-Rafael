package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.CMOIndicator;

@Component
public class MomCmo20DltCalc implements DescribableCalc {

    public static double calculate(CMOIndicator cmo, int index) {
        return cmo.getValue(index).doubleValue() - cmo.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_cmo_20_dlt",
                "CMO(20) Delta",
                "momentum",
                "CMO(20)[t] - CMO(20)[t-1]",
                "Variacao absoluta do CMO(20) entre o candle atual e o anterior.",
                "unbounded (tipicamente -40 a 40)",
                "CMO(20) caiu de 20 para 5 -> delta = -15.0"
        );
    }
}
