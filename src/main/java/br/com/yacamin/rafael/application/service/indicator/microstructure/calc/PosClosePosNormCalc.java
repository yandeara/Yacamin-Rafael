package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class PosClosePosNormCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double hl = high - low;
        if (hl < EPS) return 0;
        return (close - low) / hl;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_close_pos_norm",
                "Close Position Normalizada",
                "microstructure",
                "(close - low) / (high - low)",
                "Posicao relativa do fechamento dentro do range do candle. " +
                "0 = fechou na minima, 1 = fechou na maxima. Indica dominio de compradores ou vendedores.",
                "0 to 1",
                "close=101.5, high=102, low=100 -> (101.5-100)/(102-100) = 0.75"
        );
    }
}
