package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class RangeAtrRatioCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RangeExtension range, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < EPS) return 0;
        return range.getValue(index).doubleValue() / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_atr_ratio",
                "Range ATR Ratio",
                "microstructure",
                "range / ATR",
                "Razao entre range e ATR. " +
                "Chave de compatibilidade alternativa ao mic_range_atrn; usado em pipelines legados de feature engineering.",
                "0+",
                "range=300, atr=200 -> 300/200 = 1.5"
        );
    }
}
