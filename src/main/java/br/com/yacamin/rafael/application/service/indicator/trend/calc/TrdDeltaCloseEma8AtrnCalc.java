package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.num.Num;

@Component
public class TrdDeltaCloseEma8AtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(Indicator<Num> close, Indicator<Num> ema, ATRIndicator atr14, int index) {
        double delta = close.getValue(index).doubleValue() - ema.getValue(index).doubleValue();
        double atr = atr14.getValue(index).doubleValue();
        if (Math.abs(atr) < EPS) return 0.0;
        return delta / atr;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_delta_close_ema_8_atrn", "Delta Close-EMA8 ATR-N", "trend", "(close - EMA8) / ATR14",
                "Delta assinado entre close e EMA 8 normalizado por ATR.",
                "unbounded", "close=102, EMA8=100, ATR=50 -> 2/50 = 0.04"
        );
    }
}
