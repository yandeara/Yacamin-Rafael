package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.WickSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class WickImbalanceSlpW10Calc implements DescribableCalc {

    public static double calculate(WickSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_wick_imbalance_slp_w10",
                "Wick Imbalance Slope W10",
                "microstructure",
                "OLS_slope(wick_imbalance, 10)",
                "Inclinacao da regressao linear do desequilibrio de pavios em 10 periodos. " +
                "Positivo indica tendencia crescente de dominancia do pavio superior.",
                "unbounded",
                "imbalance crescendo de 0.1 a 0.5 em 10 barras -> slope positivo"
        );
    }
}
