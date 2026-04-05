package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Component
public class TrdRatioEma2050Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(EMAIndicator ema20, EMAIndicator ema50, int index) {
        double a = ema20.getValue(index).doubleValue();
        double b = ema50.getValue(index).doubleValue();
        if (Math.abs(b) < EPS) return 0.0;
        return a / b;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_ratio_ema_20_50", "Ratio EMA20/EMA50", "trend", "EMA20 / EMA50",
                "Razao entre EMA 20 e EMA 50. Valores > 1 indicam tendencia de alta de medio prazo.",
                "0+", "EMA20=105, EMA50=100 -> 1.05"
        );
    }
}
