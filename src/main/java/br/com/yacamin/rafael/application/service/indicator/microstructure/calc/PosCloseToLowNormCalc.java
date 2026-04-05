package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class PosCloseToLowNormCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double range = high - low;
        if (range < EPS) return 0;
        return (close - low) / range;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_close_to_low_norm",
                "Close to Low Normalizado",
                "microstructure",
                "(close - low) / range",
                "Distancia do fechamento ate a minima, normalizada pelo range. " +
                "Valores altos indicam suporte na minima (pressao compradora), baixos indicam fechamento proximo do fundo.",
                "0 to 1",
                "close=101, low=100, high=102 -> (101-100)/(102-100) = 0.5"
        );
    }
}
