package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import org.springframework.stereotype.Component;
@Component
public class VltAtr14SlpCalc implements DescribableCalc {
    public static double calculate(LinearRegressionSlopeIndicator slp, int index) { return slp.getValue(index).doubleValue(); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_atr_14_slp", "ATR 14 Slope", "volatility", "slope(ATR14)", "Slope do ATR14.", "unbounded", ""); }
}
