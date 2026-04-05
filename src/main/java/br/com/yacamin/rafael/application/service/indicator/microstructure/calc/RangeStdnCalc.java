package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeStdExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeStdnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RangeExtension range, RangeStdExtension std20, int index) {
        double stdVal = std20.getValue(index).doubleValue();
        if (Math.abs(stdVal) < EPS) return 0;
        return range.getValue(index).doubleValue() / stdVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_stdn",
                "Range Std-Normalized",
                "microstructure",
                "range / std(range, 20)",
                "Range normalizado pelo desvio padrao do range em 20 barras. " +
                "Funciona como z-score do range; valores altos indicam barra extrema relativa a dispersao recente.",
                "0+",
                "range=4.0, std20=1.0 -> 4.0/1.0 = 4.0"
        );
    }
}
