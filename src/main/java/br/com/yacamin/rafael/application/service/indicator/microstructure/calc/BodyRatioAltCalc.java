package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyRatioExtension;

import org.springframework.stereotype.Component;

@Component
public class BodyRatioAltCalc implements DescribableCalc {

    public static double calculate(BodyRatioExtension ratio, int index) {
        return ratio.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_body_ratio",
                "Body Ratio Alt",
                "microstructure",
                "body / range (calculo alternativo)",
                "Razao corpo/range. " +
                "Alias para compatibilidade com feature maps antigos.",
                "-1 to 1",
                "close=101, open=100, high=102, low=99 -> (101-100)/(102-99) = 0.333"
        );
    }
}
