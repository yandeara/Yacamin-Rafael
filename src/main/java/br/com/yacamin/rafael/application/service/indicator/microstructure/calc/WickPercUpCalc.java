package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.UpperWickExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickPercUpCalc implements DescribableCalc {

    public static double calculate(UpperWickExtension upper, BarSeries series, int index) {
        return WickUpperPctCalc.calculate(upper, series, index);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_wick_perc_up",
                "Wick Perc Up",
                "microstructure",
                "upper_wick / (high - low)",
                "Alias de upper_wick_pct. Percentual do range ocupado pelo pavio superior. " +
                "Util para medir pressao vendedora no topo do candle.",
                "0-1",
                "upper_wick=1.0, range=3.0 -> 1.0/3.0 = 0.333"
        );
    }
}
