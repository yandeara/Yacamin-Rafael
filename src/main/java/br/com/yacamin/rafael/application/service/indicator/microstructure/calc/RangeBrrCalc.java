package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class RangeBrrCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        var bar = series.getBar(index);
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double open = bar.getOpenPrice().doubleValue();
        double hl = high - low;
        if (Math.abs(hl) < EPS) return 0;
        return Math.abs(close - open) / hl;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_brr",
                "Body-Range Ratio",
                "microstructure",
                "|close - open| / (high - low)",
                "Razao entre corpo absoluto e range total do candle. " +
                "Valores proximos de 1.0 indicam candle sem pavios (forte direcionalidade); proximos de 0 indicam doji/indecisao.",
                "0-1",
                "close=101, open=99, high=102, low=98 -> |2|/4 = 0.5"
        );
    }
}
