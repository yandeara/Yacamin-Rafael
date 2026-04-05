package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.TrueRangeExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeGapRatioCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(TrueRangeExtension tr, RangeExtension range, int index) {
        double trVal = tr.getValue(index).doubleValue();
        if (Math.abs(trVal) < EPS) return 0;
        return (trVal - range.getValue(index).doubleValue()) / trVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_gap_ratio",
                "Gap Ratio",
                "microstructure",
                "(TR - range) / TR",
                "Proporcao do true range que corresponde ao gap. " +
                "Valores proximos de 0 indicam ausencia de gap; valores altos indicam que o gap dominou a volatilidade da barra.",
                "0-1",
                "tr=300, range=200 -> (300-200)/300 = 0.333"
        );
    }
}
