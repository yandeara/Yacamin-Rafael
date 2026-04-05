package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
@Component
public class VltStd14_48CmprCalc implements DescribableCalc {
    public static double calculate(StandardDeviationIndicator stdS, StandardDeviationIndicator stdL, int index) {
        double ratio = stdS.getValue(index).doubleValue() / stdL.getValue(index).doubleValue();
        return Math.max(1.0 - ratio, 0.0);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_std_14_48_cmpr", "STD 14/48 Cmpr", "volatility", "max(1-ratio,0)", "Compressao STD 14 vs 48.", "0+", ""); }
}
