package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class PosCloseOpenNormCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double open = series.getBar(index).getOpenPrice().doubleValue();
        if (Math.abs(open) < EPS) return 0;
        return (close - open) / open;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_close_open_norm",
                "Close/Open Normalizado",
                "microstructure",
                "(close - open) / open",
                "Retorno percentual do candle normalizado pelo preco de abertura. " +
                "Equivale ao retorno simples intra-candle, util para comparar magnitude entre periodos.",
                "unbounded (tipicamente -0.05 a 0.05)",
                "close=101.0, open=100.0 -> (101-100)/100 = 0.01"
        );
    }
}
