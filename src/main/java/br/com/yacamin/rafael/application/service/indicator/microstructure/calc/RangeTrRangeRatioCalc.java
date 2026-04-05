package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.TrueRangeExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeTrRangeRatioCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(TrueRangeExtension tr, RangeExtension range, int index) {
        double rangeVal = range.getValue(index).doubleValue();
        if (Math.abs(rangeVal) < EPS) return 0;
        return tr.getValue(index).doubleValue() / rangeVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_tr_range_ratio",
                "TR Range Ratio",
                "microstructure",
                "TR / range",
                "Razao entre true range e range simples. " +
                "Valores proximos de 1.0 indicam ausencia de gap; valores > 1.0 indicam presenca de gap significativo entre barras.",
                "1+",
                "tr=300, range=200 -> 300/200 = 1.5"
        );
    }
}
