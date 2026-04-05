package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class TrdEma20SlpAtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(LinearRegressionSlopeIndicator slope, ATRIndicator atr14, int index) {
        double s = slope.getValue(index).doubleValue();
        double atr = atr14.getValue(index).doubleValue();
        if (Math.abs(atr) < EPS) return 0.0;
        return s / atr;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_ema_20_slp_atrn", "EMA 20 Slope ATR-Normalized", "trend", "slope(EMA20) / ATR14",
                "Slope da EMA 20 normalizado pela volatilidade (ATR14).",
                "unbounded", "slope=10, ATR=100 -> 0.1"
        );
    }
}
