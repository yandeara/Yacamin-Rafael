package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.TrueRangeExtension;

import org.springframework.stereotype.Component;

@Component
public class TrueRangeCalc implements DescribableCalc {

    public static double calculate(TrueRangeExtension tr, int index) {
        return tr.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_true_range",
                "True Range",
                "microstructure",
                "max(high-low, |high-prevClose|, |low-prevClose|)",
                "True Range incorpora gaps entre barras na medicao de volatilidade. " +
                "Valores maiores que o range simples indicam presenca de gap significativo.",
                "0+",
                "high=101, low=99, prevClose=98 -> max(2, 3, 1) = 3.0"
        );
    }
}
