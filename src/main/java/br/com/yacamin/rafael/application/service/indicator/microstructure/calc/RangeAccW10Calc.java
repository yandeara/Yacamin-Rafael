package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeAccW10Calc implements DescribableCalc {

    public static double calculate(RangeExtension range, int index) {
        if (index < 10) return 0;
        return range.getValue(index).doubleValue() - range.getValue(index - 10).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_acc_w10",
                "Range Acceleration W10",
                "microstructure",
                "range[t] - range[t-10]",
                "Diferenca absoluta do range em 10 barras. " +
                "Mede a aceleracao da volatilidade em janela mais ampla, identificando mudancas de regime.",
                "unbounded",
                "range[t]=4.0, range[t-10]=2.0 -> 4.0 - 2.0 = 2.0"
        );
    }
}
