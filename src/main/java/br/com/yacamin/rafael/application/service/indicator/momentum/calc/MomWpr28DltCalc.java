package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.WilliamsRIndicator;

@Component
public class MomWpr28DltCalc implements DescribableCalc {

    public static double calculate(WilliamsRIndicator wpr, int index) {
        return wpr.getValue(index).doubleValue() - wpr.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_wpr_28_dlt",
                "WPR(28) Delta",
                "momentum",
                "WPR(28)[t] - WPR(28)[t-1]",
                "Variacao absoluta do WPR(28) entre o candle atual e o anterior.",
                "unbounded (tipicamente -40 a 40)",
                "WPR(28) subiu de -80 para -60 -> delta = 20.0"
        );
    }
}
