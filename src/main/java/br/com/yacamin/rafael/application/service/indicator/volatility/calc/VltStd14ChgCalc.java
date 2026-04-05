package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
@Component
public class VltStd14ChgCalc implements DescribableCalc {
    public static double calculate(StandardDeviationIndicator std, int index) {
        double curr = std.getValue(index).doubleValue();
        double prev = std.getValue(index - 1).doubleValue();
        return (curr / prev) - 1.0;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_std_14_chg", "STD 14 Chg", "volatility", "(std_t/std_t-1)-1", "Taxa de mudanca STD 14.", "unbounded", ""); }
}
