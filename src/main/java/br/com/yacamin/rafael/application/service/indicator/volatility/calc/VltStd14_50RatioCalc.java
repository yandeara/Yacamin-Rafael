package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
@Component
public class VltStd14_50RatioCalc implements DescribableCalc {
    public static double calculate(StandardDeviationIndicator stdS, StandardDeviationIndicator stdL, int index) {
        return stdS.getValue(index).doubleValue() / stdL.getValue(index).doubleValue();
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_std_14_50_ratio", "STD 14/50 Ratio", "volatility", "std14/std50", "Razao STD 14 vs 50.", "0+", ""); }
}
