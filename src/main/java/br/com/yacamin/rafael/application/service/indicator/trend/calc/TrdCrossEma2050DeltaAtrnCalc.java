package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Component
public class TrdCrossEma2050DeltaAtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(EMAIndicator fast, EMAIndicator slow, double close, ATRIndicator atr14, int index) {
        double fNow = fast.getValue(index).doubleValue();
        double sNow = slow.getValue(index).doubleValue();
        if (Math.abs(close) < EPS) return 0.0;
        double d = (fNow - sNow) / close;
        double atr = atr14.getValue(index).doubleValue();
        if (Math.abs(atr) < EPS) return 0.0;
        return d / atr;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_cross_ema_20_50_delta_atrn", "Cross EMA20/EMA50 Delta ATR-N", "trend", "((EMA20 - EMA50) / close) / ATR14",
                "Delta do cruzamento EMA 20/50 normalizado por ATR.",
                "unbounded", "delta=0.05, ATR=100 -> 0.0005"
        );
    }
}
