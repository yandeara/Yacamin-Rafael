package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import org.springframework.stereotype.Component;
@Component
public class VltAtr7SlpCalc implements DescribableCalc {
    public static double calculate(LinearRegressionSlopeIndicator slp, int index) { return slp.getValue(index).doubleValue(); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_atr_7_slp", "ATR 7 Slope", "volatility", "slope(ATR7)", "Slope do ATR7.", "unbounded", ""); }
}
