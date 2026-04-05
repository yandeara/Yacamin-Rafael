package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class MomClose8SlpAtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double slopeValue, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < EPS) return 0.0;
        return slopeValue / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_close_8_slp_atrn",
                "Close Slope 8 ATR-Normalized",
                "momentum",
                "slope(close, 8) / ATR(14)",
                "Inclinacao da regressao linear do close (8 periodos) normalizada pelo ATR(14).",
                "R",
                "slope=3.0, ATR=50 -> 3.0/50 = 0.06"
        );
    }
}
