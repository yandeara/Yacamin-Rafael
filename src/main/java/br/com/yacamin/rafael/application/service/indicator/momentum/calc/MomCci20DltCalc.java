package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.CCIIndicator;

@Component
public class MomCci20DltCalc implements DescribableCalc {

    public static double calculate(CCIIndicator cci, int index) {
        return cci.getValue(index).doubleValue() - cci.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_cci_20_dlt",
                "CCI(20) Delta",
                "momentum",
                "CCI(20)[t] - CCI(20)[t-1]",
                "Variacao absoluta do CCI(20) entre o candle atual e o anterior.",
                "unbounded",
                "CCI(20) subiu de 80 para 120 -> delta = 40.0"
        );
    }
}
