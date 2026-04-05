package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import org.springframework.stereotype.Component;
@Component
public class VltStd20SlpCalc implements DescribableCalc {
    public static double calculate(LinearRegressionSlopeIndicator slp, int index) {
        return slp.getValue(index).doubleValue();
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_std_20_slp", "STD 20 Slp", "volatility", "slope(std, 20)", "Slope STD 20.", "unbounded", ""); }
}
