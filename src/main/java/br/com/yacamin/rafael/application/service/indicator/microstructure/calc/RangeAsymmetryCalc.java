package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class RangeAsymmetryCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        var bar = series.getBar(index);
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double denom = close - low;
        if (Math.abs(denom) < EPS) return 0;
        return (high - close) / denom;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_asymmetry",
                "Range Asymmetry",
                "microstructure",
                "(high - close) / (close - low)",
                "Assimetria do range em relacao ao close. " +
                "Valores > 1 indicam pavio superior maior (pressao vendedora no topo); < 1 indicam pavio inferior maior (pressao compradora na baixa).",
                "0+",
                "high=102, close=100.5, low=99 -> 1.5/1.5 = 1.0"
        );
    }
}
