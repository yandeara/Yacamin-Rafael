package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;
@Component
public class VltAtr7ChgCalc implements DescribableCalc {
    public static double calculate(ATRIndicator atr, int index) { double curr = atr.getValue(index).doubleValue(); double prev = atr.getValue(index - 1).doubleValue(); return (curr / prev) - 1.0; }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_atr_7_chg", "ATR 7 Change", "volatility", "(atr[t]/atr[t-1])-1", "Variacao pct do ATR7.", "unbounded", ""); }
}
