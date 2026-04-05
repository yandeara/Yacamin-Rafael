package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class MomClose3SlpAtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double slopeValue, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < EPS) return 0.0;
        return slopeValue / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_close_3_slp_atrn",
                "Close Slope 3 ATR-Normalized",
                "momentum",
                "slope(close, 3) / ATR(14)",
                "Inclinacao da regressao linear do close (3 periodos) normalizada pelo ATR(14).",
                "R",
                "slope=2.5, ATR=50 -> 2.5/50 = 0.05"
        );
    }
}
