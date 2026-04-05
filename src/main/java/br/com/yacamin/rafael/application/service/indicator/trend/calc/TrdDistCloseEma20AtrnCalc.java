package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.num.Num;

@Component
public class TrdDistCloseEma20AtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(Indicator<Num> close, Indicator<Num> ema, ATRIndicator atr14, int index) {
        double dist = Math.abs(close.getValue(index).doubleValue() - ema.getValue(index).doubleValue());
        double atr = atr14.getValue(index).doubleValue();
        if (Math.abs(atr) < EPS) return 0.0;
        return dist / atr;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_dist_close_ema_20_atrn", "Distance Close-EMA20 ATR-N", "trend", "|close - EMA20| / ATR14",
                "Distancia absoluta entre close e EMA 20 normalizada por ATR.",
                "0+", "close=100, EMA20=95, ATR=50 -> |5|/50 = 0.1"
        );
    }
}
