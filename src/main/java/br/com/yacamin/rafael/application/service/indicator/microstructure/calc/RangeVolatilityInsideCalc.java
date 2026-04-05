package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeLaggedMeanExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeVolatilityInsideCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RangeExtension range, RangeLaggedMeanExtension laggedMean, int index) {
        double meanVal = laggedMean.getValue(index).doubleValue();
        if (Math.abs(meanVal) < EPS) return 0;
        return range.getValue(index).doubleValue() / meanVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_volatility_inside",
                "Candle Volatility Inside",
                "microstructure",
                "range / laggedMean(range)",
                "Range atual normalizado pela media defasada do range. " +
                "Valores acima de 1.0 indicam barra com amplitude acima do historico recente; abaixo de 1.0 indica compressao.",
                "0+",
                "range=3.0, laggedMean=2.0 -> 3.0/2.0 = 1.5"
        );
    }
}
