package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyAbsSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class BodySlpW20Calc implements DescribableCalc {

    public static double calculate(BodyAbsSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_body_slp_w20",
                "Body Abs Slope W20",
                "microstructure",
                "linreg_slope(|body|, 20)",
                "Inclinacao da regressao linear do corpo absoluto em janela de 20 periodos. " +
                "Captura tendencias de prazo mais longo no tamanho dos corpos dos candles.",
                "unbounded",
                "|body| crescendo de 0.2 a 0.9 em 20 candles -> slope ~ 0.037"
        );
    }
}
