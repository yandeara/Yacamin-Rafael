package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.WilliamsRIndicator;

@Component
public class MomWpr14DltCalc implements DescribableCalc {

    public static double calculate(WilliamsRIndicator wpr, int index) {
        return wpr.getValue(index).doubleValue() - wpr.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_wpr_14_dlt",
                "WPR(14) Delta",
                "momentum",
                "WPR(14)[t] - WPR(14)[t-1]",
                "Variacao absoluta do WPR(14) entre o candle atual e o anterior.",
                "unbounded (tipicamente -40 a 40)",
                "WPR(14) subiu de -80 para -60 -> delta = 20.0"
        );
    }
}
