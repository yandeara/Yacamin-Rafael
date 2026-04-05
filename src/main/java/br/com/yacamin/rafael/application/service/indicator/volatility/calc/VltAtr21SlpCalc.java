package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import org.springframework.stereotype.Component;
@Component
public class VltAtr21SlpCalc implements DescribableCalc {
    public static double calculate(LinearRegressionSlopeIndicator slp, int index) { return slp.getValue(index).doubleValue(); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_atr_21_slp", "ATR 21 Slope", "volatility", "slope(ATR21)", "Slope do ATR21.", "unbounded", ""); }
}
