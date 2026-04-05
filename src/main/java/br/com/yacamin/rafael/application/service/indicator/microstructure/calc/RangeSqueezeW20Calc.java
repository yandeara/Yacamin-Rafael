package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeSmaExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeSqueezeW20Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RangeExtension range, RangeSmaExtension sma20, int index) {
        double smaVal = sma20.getValue(index).doubleValue();
        if (Math.abs(smaVal) < EPS) return 0;
        return range.getValue(index).doubleValue() / smaVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_squeeze_w20",
                "Range Squeeze W20",
                "microstructure",
                "range / SMA(range, 20)",
                "Range atual normalizado pela media de 20 barras. " +
                "Valores < 1.0 indicam squeeze (barra comprimida relativa ao normal); > 1.0 indica expansao alem do esperado.",
                "0+",
                "range=1.5, sma20=2.0 -> 1.5/2.0 = 0.75 (squeeze)"
        );
    }
}
