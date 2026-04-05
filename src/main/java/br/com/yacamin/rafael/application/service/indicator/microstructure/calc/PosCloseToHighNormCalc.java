package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class PosCloseToHighNormCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double range = high - low;
        if (range < EPS) return 0;
        return (high - close) / range;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_close_to_high_norm",
                "Close to High Normalizado",
                "microstructure",
                "(high - close) / range",
                "Distancia do fechamento ate a maxima, normalizada pelo range. " +
                "Valores altos indicam rejeicao no topo (pressao vendedora), baixos indicam fechamento proximo da maxima.",
                "0 to 1",
                "high=102, close=101, low=100 -> (102-101)/(102-100) = 0.5"
        );
    }
}
