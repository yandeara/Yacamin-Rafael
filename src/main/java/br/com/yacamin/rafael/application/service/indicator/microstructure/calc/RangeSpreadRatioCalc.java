package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class RangeSpreadRatioCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RangeExtension range, BarSeries series, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        if (Math.abs(close) < EPS) return 0;
        return range.getValue(index).doubleValue() / close;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_spread_ratio",
                "Candle Spread Ratio",
                "microstructure",
                "range / close",
                "Range normalizado pelo preco de fechamento. " +
                "Mede a amplitude relativa ao nivel de preco, permitindo comparacao entre periodos com precos diferentes.",
                "0+",
                "range=200, close=40000 -> 200/40000 = 0.005"
        );
    }
}
