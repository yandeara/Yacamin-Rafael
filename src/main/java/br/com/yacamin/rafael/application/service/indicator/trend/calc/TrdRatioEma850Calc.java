package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Component
public class TrdRatioEma850Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(EMAIndicator ema8, EMAIndicator ema50, int index) {
        double a = ema8.getValue(index).doubleValue();
        double b = ema50.getValue(index).doubleValue();
        if (Math.abs(b) < EPS) return 0.0;
        return a / b;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_ratio_ema_8_50", "Ratio EMA8/EMA50", "trend", "EMA8 / EMA50",
                "Razao entre EMA 8 e EMA 50. Mede expansao total do ribbon rapido vs medio.",
                "0+", "EMA8=108, EMA50=100 -> 1.08"
        );
    }
}
