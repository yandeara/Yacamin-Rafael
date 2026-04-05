package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
@Component
public class VltStd14_50ExpnCalc implements DescribableCalc {
    public static double calculate(StandardDeviationIndicator stdS, StandardDeviationIndicator stdL, int index) {
        double ratio = stdS.getValue(index).doubleValue() / stdL.getValue(index).doubleValue();
        return Math.max(ratio - 1.0, 0.0);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_std_14_50_expn", "STD 14/50 Expn", "volatility", "max(ratio-1,0)", "Expansao STD 14 vs 50.", "0+", ""); }
}
