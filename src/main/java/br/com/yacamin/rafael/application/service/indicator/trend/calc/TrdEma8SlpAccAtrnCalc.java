package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class TrdEma8SlpAccAtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(DifferenceIndicator acc, ATRIndicator atr14, int index) {
        double a = acc.getValue(index).doubleValue();
        double atr = atr14.getValue(index).doubleValue();
        if (Math.abs(atr) < EPS) return 0.0;
        return a / atr;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_ema_8_slp_acc_atrn", "EMA 8 Slope Acc ATR-Normalized", "trend", "slopeAcc(EMA8) / ATR14",
                "Aceleracao do slope da EMA 8 normalizada pela volatilidade.",
                "unbounded", "acc=2, ATR=100 -> 0.02"
        );
    }
}
