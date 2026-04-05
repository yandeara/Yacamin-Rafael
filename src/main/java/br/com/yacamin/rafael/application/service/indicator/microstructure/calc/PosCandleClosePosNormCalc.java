package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class PosCandleClosePosNormCalc implements DescribableCalc {

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
                "mic_candle_close_pos_norm",
                "Candle Close Position Normalizada",
                "microstructure",
                "(close - low) / (high - low)",
                "Posicao relativa do fechamento no range do candle (alias candle-centric). " +
                "Complementa mic_close_pos_norm para uso em contextos de analise de candle.",
                "0 to 1",
                "close=101.5, high=102, low=100 -> (101.5-100)/(102-100) = 0.75"
        );
    }
}
