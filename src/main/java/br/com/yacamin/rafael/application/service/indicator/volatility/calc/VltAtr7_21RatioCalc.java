package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;
@Component
public class VltAtr7_21RatioCalc implements DescribableCalc {
    public static double calculate(ATRIndicator fast, ATRIndicator slow, int index) { return fast.getValue(index).doubleValue() / slow.getValue(index).doubleValue(); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_atr_7_21_ratio", "ATR 7_21 Ratio", "volatility", "ATR7/ATR21", "Razao ATR7/ATR21.", "0+", ""); }
}
