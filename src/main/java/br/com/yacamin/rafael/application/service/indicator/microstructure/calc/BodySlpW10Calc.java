package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyAbsSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class BodySlpW10Calc implements DescribableCalc {

    public static double calculate(BodyAbsSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_body_slp_w10",
                "Body Abs Slope W10",
                "microstructure",
                "linreg_slope(|body|, 10)",
                "Inclinacao da regressao linear do corpo absoluto em janela de 10 periodos. " +
                "Slope positivo indica corpos crescentes, sugerindo aumento de convicao direcional.",
                "unbounded",
                "|body| crescendo de 0.3 a 0.8 em 10 candles -> slope ~ 0.055"
        );
    }
}
