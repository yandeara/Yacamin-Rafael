package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.RealizedVolExtension;
import org.springframework.stereotype.Component;
@Component
public class VltVolRv10_30RatioCalc implements DescribableCalc {
    public static double calculate(RealizedVolExtension rvS, RealizedVolExtension rvL, int index) {
        return rvS.getValue(index).doubleValue() / rvL.getValue(index).doubleValue();
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_vol_rv_10_30_ratio", "RV 10/30 Ratio", "volatility", "rv10/rv30", "Razao RV 10 vs 30.", "0+", ""); }
}
