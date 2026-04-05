package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.num.Num;

@Component
public class TrdDeltaEma2050AtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(Indicator<Num> ema20, Indicator<Num> ema50, ATRIndicator atr14, int index) {
        double delta = ema20.getValue(index).doubleValue() - ema50.getValue(index).doubleValue();
        double atr = atr14.getValue(index).doubleValue();
        if (Math.abs(atr) < EPS) return 0.0;
        return delta / atr;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_delta_ema_20_50_atrn", "Delta EMA20-EMA50 ATR-N", "trend", "(EMA20 - EMA50) / ATR14",
                "Delta assinado entre EMA 20 e EMA 50 normalizado por ATR.",
                "unbounded", "delta=5, ATR=50 -> 0.1"
        );
    }
}
