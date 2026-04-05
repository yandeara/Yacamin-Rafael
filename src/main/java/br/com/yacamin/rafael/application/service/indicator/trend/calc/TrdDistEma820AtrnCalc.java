package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.num.Num;

@Component
public class TrdDistEma820AtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(Indicator<Num> ema8, Indicator<Num> ema20, ATRIndicator atr14, int index) {
        double dist = Math.abs(ema8.getValue(index).doubleValue() - ema20.getValue(index).doubleValue());
        double atr = atr14.getValue(index).doubleValue();
        if (Math.abs(atr) < EPS) return 0.0;
        return dist / atr;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_dist_ema_8_20_atrn", "Distance EMA8-EMA20 ATR-N", "trend", "|EMA8 - EMA20| / ATR14",
                "Distancia absoluta entre EMA 8 e EMA 20 normalizada por ATR. Mede separacao do ribbon de curto prazo.",
                "0+", "EMA8=100, EMA20=95, ATR=50 -> |5|/50 = 0.1"
        );
    }
}
