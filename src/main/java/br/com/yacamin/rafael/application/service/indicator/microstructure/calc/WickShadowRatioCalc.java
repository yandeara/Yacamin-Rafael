package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.UpperWickExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.LowerWickExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickShadowRatioCalc implements DescribableCalc {

    public static double calculate(UpperWickExtension upper, LowerWickExtension lower, BarSeries series, int index) {
        double u = upper.getValue(index).doubleValue();
        double l = lower.getValue(index).doubleValue();
        double totalWick = u + l;
        double close = series.getBar(index).getClosePrice().doubleValue();
        double open = series.getBar(index).getOpenPrice().doubleValue();
        double body = Math.abs(close - open);
        double denom = totalWick + body;
        if (Math.abs(denom) < 1e-12) return 0;
        return totalWick / denom;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_shadow_ratio",
                "Shadow Ratio",
                "microstructure",
                "totalWick / (totalWick + |body|)",
                "Proporcao das sombras no candle total. " +
                "1.0 = puro pavio (doji), 0.0 = puro corpo (marubozu).",
                "0-1",
                "upper=1.0, lower=1.0, |body|=2.0 -> 2.0/(2.0+2.0) = 0.5"
        );
    }
}
