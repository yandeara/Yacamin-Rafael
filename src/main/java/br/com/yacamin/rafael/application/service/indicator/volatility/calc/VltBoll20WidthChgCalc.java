package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.BollingerWidthExtension;
import org.springframework.stereotype.Component;
@Component
public class VltBoll20WidthChgCalc implements DescribableCalc {
    public static double calculate(BollingerWidthExtension width, int index) {
        double curr = width.getValue(index).doubleValue();
        double prev = width.getValue(index - 1).doubleValue();
        return (Math.abs(prev) < 1e-12) ? 0 : (curr / prev) - 1.0;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_boll_20_width_chg", "Boll 20 Width Chg", "volatility", "(w[t]/w[t-1])-1", "Variacao da largura Boll 20.", "unbounded", ""); }
}
