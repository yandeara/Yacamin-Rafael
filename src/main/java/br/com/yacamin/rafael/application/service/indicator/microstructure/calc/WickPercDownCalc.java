package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.LowerWickExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickPercDownCalc implements DescribableCalc {

    public static double calculate(LowerWickExtension lower, BarSeries series, int index) {
        return WickLowerPctCalc.calculate(lower, series, index);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_wick_perc_down",
                "Wick Perc Down",
                "microstructure",
                "lower_wick / (high - low)",
                "Alias de lower_wick_pct. Percentual do range ocupado pelo pavio inferior. " +
                "Util para medir pressao compradora no fundo do candle.",
                "0-1",
                "lower_wick=1.0, range=3.0 -> 1.0/3.0 = 0.333"
        );
    }
}
