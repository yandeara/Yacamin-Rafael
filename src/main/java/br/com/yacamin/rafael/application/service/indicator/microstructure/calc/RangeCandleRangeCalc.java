package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class RangeCandleRangeCalc implements DescribableCalc {

    public static double calculate(BarSeries series, int index) {
        var bar = series.getBar(index);
        return bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_range",
                "Candle Range",
                "microstructure",
                "high - low",
                "Amplitude do candle calculada diretamente da barra. " +
                "Duplica mic_range para compatibilidade de nomenclatura com features de candle.",
                "0+",
                "high=101.5, low=99.5 -> 101.5 - 99.5 = 2.0"
        );
    }
}
