package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi7RocCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RSIIndicator rsi, int index) {
        double prev = rsi.getValue(index - 2).doubleValue();
        double curr = rsi.getValue(index).doubleValue();
        if (Math.abs(prev) < EPS) return 0.0;
        return (curr - prev) / prev;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_7_roc",
                "RSI(7) ROC(2)",
                "momentum",
                "(RSI(7)[t] - RSI(7)[t-2]) / RSI(7)[t-2]",
                "Rate of Change do RSI(7) com lookback de 2 barras. Mede a taxa de variacao percentual do RSI.",
                "unbounded",
                "RSI(7) era 50 e agora e 55 -> ROC = 0.10"
        );
    }
}
