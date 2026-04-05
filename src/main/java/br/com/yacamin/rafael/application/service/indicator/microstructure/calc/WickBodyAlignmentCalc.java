package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.WickImbalanceExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickBodyAlignmentCalc implements DescribableCalc {

    public static double calculate(WickImbalanceExtension imb, BarSeries series, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double open = series.getBar(index).getOpenPrice().doubleValue();
        double body = close - open;
        double sign = body > 0 ? 1.0 : (body < 0 ? -1.0 : 0.0);
        return sign * imb.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_wick_body_alignment",
                "Wick Body Alignment",
                "microstructure",
                "sign(body) * wick_imbalance",
                "Alinhamento entre direcao do corpo e desequilibrio dos pavios. " +
                "Valores positivos indicam pavios reforando a direcao do corpo.",
                "-1 to 1",
                "body bullish, imbalance=0.5 -> 1.0 * 0.5 = 0.5"
        );
    }
}
