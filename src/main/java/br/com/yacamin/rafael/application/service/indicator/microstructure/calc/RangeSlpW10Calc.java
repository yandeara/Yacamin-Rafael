package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeSlpW10Calc implements DescribableCalc {

    public static double calculate(RangeSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_slp_w10",
                "Range Slope W10",
                "microstructure",
                "linreg_slope(range, 10)",
                "Inclinacao da regressao linear do range nas ultimas 10 barras. " +
                "Slope positivo indica expansao de volatilidade, negativo indica compressao.",
                "unbounded",
                "range crescendo de 1.0 a 2.0 em 10 barras -> slope ~ 0.11"
        );
    }
}
