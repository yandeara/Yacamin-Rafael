package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class RangeLmrCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        var bar = series.getBar(index);
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double hl = high - low;
        if (Math.abs(hl) < EPS) return 0;
        return (close - (high + low) / 2.0) / hl;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_lmr",
                "Location Midpoint Ratio",
                "microstructure",
                "(close - (high+low)/2) / (high-low)",
                "Posicao relativa do close em relacao ao midpoint do range. " +
                "Valores positivos indicam fechamento na metade superior; negativos na inferior. Range [-0.5, 0.5].",
                "-0.5 a 0.5",
                "close=101, high=102, low=98 -> (101-100)/4 = 0.25"
        );
    }
}
