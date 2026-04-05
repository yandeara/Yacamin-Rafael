package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.UpperWickExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.wick.extension.LowerWickExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class WickTotalAtrnCalc implements DescribableCalc {

    public static double calculate(UpperWickExtension upper, LowerWickExtension lower, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < 1e-12) return 0;
        double total = upper.getValue(index).doubleValue() + lower.getValue(index).doubleValue();
        return total / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_total_wick_atrn",
                "Total Wick ATR Norm",
                "microstructure",
                "(upper + lower) / ATR",
                "Soma dos pavios normalizada pela volatilidade ATR. " +
                "Valores altos indicam sombras grandes relativas ao regime de volatilidade atual.",
                "0+",
                "upper=1.5, lower=0.5, ATR=2.0 -> 2.0/2.0 = 1.0"
        );
    }
}
