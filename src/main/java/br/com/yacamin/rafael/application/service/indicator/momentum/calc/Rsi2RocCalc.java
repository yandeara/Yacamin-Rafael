package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi2RocCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RSIIndicator rsi, int index) {
        double prev = rsi.getValue(index - 1).doubleValue();
        double curr = rsi.getValue(index).doubleValue();
        if (Math.abs(prev) < EPS) return 0.0;
        return (curr - prev) / prev;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_2_roc",
                "RSI(2) ROC(1)",
                "momentum",
                "(RSI(2)[t] - RSI(2)[t-1]) / RSI(2)[t-1]",
                "Rate of Change do RSI(2) com lookback de 1 barra. Mede a taxa de variacao percentual do RSI.",
                "unbounded",
                "RSI(2) era 50 e agora e 55 -> ROC = 0.10"
        );
    }
}
