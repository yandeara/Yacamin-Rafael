package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeAccW5Calc implements DescribableCalc {

    public static double calculate(RangeExtension range, int index) {
        if (index < 5) return 0;
        return range.getValue(index).doubleValue() - range.getValue(index - 5).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_acc_w5",
                "Range Acceleration W5",
                "microstructure",
                "range[t] - range[t-5]",
                "Diferenca absoluta do range em 5 barras. " +
                "Mede a aceleracao da volatilidade no curtissimo prazo; valores positivos indicam expansao rapida.",
                "unbounded",
                "range[t]=3.0, range[t-5]=1.5 -> 3.0 - 1.5 = 1.5"
        );
    }
}
