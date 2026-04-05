package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi14RocCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RSIIndicator rsi, int index) {
        double prev = rsi.getValue(index - 3).doubleValue();
        double curr = rsi.getValue(index).doubleValue();
        if (Math.abs(prev) < EPS) return 0.0;
        return (curr - prev) / prev;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_14_roc",
                "RSI(14) ROC(3)",
                "momentum",
                "(RSI(14)[t] - RSI(14)[t-3]) / RSI(14)[t-3]",
                "Rate of Change do RSI(14) com lookback de 3 barras. Mede a taxa de variacao percentual do RSI.",
                "unbounded",
                "RSI(14) era 50 e agora e 55 -> ROC = 0.10"
        );
    }
}
