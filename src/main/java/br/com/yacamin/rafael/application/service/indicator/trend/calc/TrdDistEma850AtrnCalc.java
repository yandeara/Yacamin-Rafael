package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.num.Num;

@Component
public class TrdDistEma850AtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(Indicator<Num> ema8, Indicator<Num> ema50, ATRIndicator atr14, int index) {
        double dist = Math.abs(ema8.getValue(index).doubleValue() - ema50.getValue(index).doubleValue());
        double atr = atr14.getValue(index).doubleValue();
        if (Math.abs(atr) < EPS) return 0.0;
        return dist / atr;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_dist_ema_8_50_atrn", "Distance EMA8-EMA50 ATR-N", "trend", "|EMA8 - EMA50| / ATR14",
                "Distancia absoluta entre EMA 8 e EMA 50 normalizada por ATR. Mede abertura total do ribbon.",
                "0+", "EMA8=100, EMA50=85, ATR=50 -> |15|/50 = 0.3"
        );
    }
}
