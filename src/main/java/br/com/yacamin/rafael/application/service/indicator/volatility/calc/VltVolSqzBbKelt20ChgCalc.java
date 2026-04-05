package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.BollingerWidthExtension;
import org.ta4j.core.indicators.ATRIndicator;
import org.springframework.stereotype.Component;
@Component
public class VltVolSqzBbKelt20ChgCalc implements DescribableCalc {
    public static double calculate(BollingerWidthExtension bbW, ATRIndicator atr, int index) {
        double curr = SqzHelper.sqz(bbW, atr, index);
        double prev = SqzHelper.sqz(bbW, atr, index - 1);
        if (Math.abs(prev) < 1e-12) return 0;
        return (curr / prev) - 1.0;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_vol_sqz_bb_kelt_20_chg", "Squeeze Chg 20", "volatility", "sqz(t)/sqz(t-1)-1", "Squeeze change.", "unbounded", ""); }
}
