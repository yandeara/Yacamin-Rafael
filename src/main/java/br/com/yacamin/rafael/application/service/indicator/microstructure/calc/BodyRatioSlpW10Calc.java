package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyRatioSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class BodyRatioSlpW10Calc implements DescribableCalc {

    public static double calculate(BodyRatioSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_body_ratio_slp_w10",
                "Body Ratio Slope W10",
                "microstructure",
                "linreg_slope(body_ratio, 10)",
                "Inclinacao da regressao linear do body ratio em janela de 10 periodos. " +
                "Slope positivo indica candles ficando mais bullish, negativo mais bearish.",
                "unbounded",
                "body_ratio subindo de -0.5 a 0.5 em 10 candles -> slope ~ 0.11"
        );
    }
}
