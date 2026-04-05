package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Component
public class TrdAligmentEma82050DeltaCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(EMAIndicator e20, EMAIndicator e50, double close, int index) {
        double m = e20.getValue(index).doubleValue();
        double s = e50.getValue(index).doubleValue();
        if (Math.abs(close) < EPS) return 0.0;
        return (m - s) / close;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_aligment_ema_8_20_50_delta", "EMA Alignment Delta", "trend", "(EMA20 - EMA50) / close",
                "Diferenca entre EMA 20 e EMA 50 normalizada pelo close. Mede magnitude do alinhamento.",
                "unbounded", "EMA20=100, EMA50=95, close=100 -> 0.05"
        );
    }
}
