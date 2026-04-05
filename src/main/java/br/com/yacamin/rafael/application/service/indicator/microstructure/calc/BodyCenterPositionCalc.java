package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class BodyCenterPositionCalc implements DescribableCalc {

    public static double calculate(BarSeries series, int index) {
        double open = series.getBar(index).getOpenPrice().doubleValue();
        double close = series.getBar(index).getClosePrice().doubleValue();
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double range = high - low;
        if (Math.abs(range) < 1e-12) return 0.5;
        return ((open + close) / 2.0 - low) / range;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_body_center_position",
                "Body Center Position",
                "microstructure",
                "((open+close)/2 - low) / range",
                "Posicao do centro do corpo dentro do range. " +
                "0.5=meio, proximo de 1=corpo no topo, proximo de 0=corpo na base.",
                "0-1",
                "open=100, close=102, high=103, low=99 -> ((100+102)/2 - 99) / (103-99) = 2.0/4.0 = 0.5"
        );
    }
}
