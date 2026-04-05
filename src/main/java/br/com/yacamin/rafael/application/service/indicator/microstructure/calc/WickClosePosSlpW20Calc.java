package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.WickSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class WickClosePosSlpW20Calc implements DescribableCalc {

    public static double calculate(WickSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_close_pos_slp_w20",
                "Close Position Slope W20",
                "microstructure",
                "OLS_slope(close_pos_norm, 20)",
                "Inclinacao da posicao normalizada do close em 20 periodos. " +
                "Positivo indica tendencia do close a fechar mais proximo da maxima.",
                "unbounded",
                "close_pos subindo de 0.3 a 0.8 em 20 barras -> slope positivo"
        );
    }
}
