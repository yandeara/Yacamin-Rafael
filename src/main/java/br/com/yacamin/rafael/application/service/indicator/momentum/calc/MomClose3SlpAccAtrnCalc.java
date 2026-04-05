package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class MomClose3SlpAccAtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(double accValue, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < EPS) return 0.0;
        return accValue / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_close_3_slp_acc_atrn",
                "Close Slope 3 Acceleration ATR-Normalized",
                "momentum",
                "slope_acc(close,3) / ATR(14)",
                "Aceleracao do slope do close (3 periodos) normalizada pelo ATR(14).",
                "R",
                "acc=0.5, ATR=50 -> 0.5/50 = 0.01"
        );
    }
}
