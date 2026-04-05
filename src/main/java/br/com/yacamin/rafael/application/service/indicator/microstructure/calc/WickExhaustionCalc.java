package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.WickImbalanceExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickExhaustionCalc implements DescribableCalc {

    public static double calculate(WickImbalanceExtension imb, BarSeries series, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double open = series.getBar(index).getOpenPrice().doubleValue();
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double range = high - low;
        double bodyRatio = (range < 1e-12) ? 0.0 : Math.abs(close - open) / range;
        return Math.abs(imb.getValue(index).doubleValue()) * (1.0 - bodyRatio);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_wick_exhaustion",
                "Wick Exhaustion",
                "microstructure",
                "|wick_imbalance| * (1 - bodyRatio)",
                "Indicador de exaustao via pavios. Combina desequilibrio dos pavios com " +
                "fraqueza do corpo. Valores altos sugerem rejeicao forte com corpo fraco.",
                "0-1",
                "|imb|=0.8, bodyRatio=0.3 -> 0.8 * 0.7 = 0.56"
        );
    }
}
