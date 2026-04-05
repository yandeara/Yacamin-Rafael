package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class TrdEma8SlpAtrnCalc implements DescribableCalc {

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
                "trd_ema_8_slp_atrn", "EMA 8 Slope ATR-Normalized", "trend", "slope(EMA8) / ATR14",
                "Slope da EMA 8 normalizado pela volatilidade (ATR14). Permite comparar forca de tendencia entre diferentes regimes de volatilidade.",
                "unbounded", "slope=10, ATR=100 -> 0.1"
        );
    }
}
