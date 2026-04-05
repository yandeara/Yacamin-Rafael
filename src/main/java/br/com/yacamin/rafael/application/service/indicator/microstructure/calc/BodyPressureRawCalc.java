package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyRatioExtension;

import org.springframework.stereotype.Component;

@Component
public class BodyPressureRawCalc implements DescribableCalc {

    public static double calculate(BodyRatioExtension ratio, int index) {
        return ratio.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_pressure_raw",
                "Body Pressure Raw",
                "microstructure",
                "body / range (alias de body_ratio)",
                "Pressao direcional bruta do candle. " +
                "Valores positivos=pressao compradora, negativos=vendedora.",
                "-1 to 1",
                "close=101, open=100, high=102, low=99 -> (101-100)/(102-99) = 0.333"
        );
    }
}
