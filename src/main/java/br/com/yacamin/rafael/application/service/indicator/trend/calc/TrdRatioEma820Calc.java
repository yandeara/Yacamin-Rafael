package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Component
public class TrdRatioEma820Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(EMAIndicator ema8, EMAIndicator ema20, int index) {
        double a = ema8.getValue(index).doubleValue();
        double b = ema20.getValue(index).doubleValue();
        if (Math.abs(b) < EPS) return 0.0;
        return a / b;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_ratio_ema_8_20", "Ratio EMA8/EMA20", "trend", "EMA8 / EMA20",
                "Razao entre EMA 8 e EMA 20. Valores > 1 indicam tendencia de alta de curto prazo.",
                "0+", "EMA8=102, EMA20=100 -> 1.02"
        );
    }
}
