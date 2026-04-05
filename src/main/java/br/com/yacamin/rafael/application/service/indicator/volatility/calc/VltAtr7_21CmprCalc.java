package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;
@Component
public class VltAtr7_21CmprCalc implements DescribableCalc {
    public static double calculate(ATRIndicator fast, ATRIndicator slow, int index) { double ratio = fast.getValue(index).doubleValue() / slow.getValue(index).doubleValue(); return Math.max(1.0 - ratio, 0.0); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_atr_7_21_cmpr", "ATR 7_21 Compression", "volatility", "max(1-ratio, 0)", "Compressao ATR7 vs ATR21.", "0+", ""); }
}
