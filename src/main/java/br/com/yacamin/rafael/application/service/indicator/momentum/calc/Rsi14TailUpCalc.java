package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi14TailUpCalc implements DescribableCalc {

    private static final double UPPER = 70.0;

    public static double calculate(RSIIndicator rsi, int index) {
        double val = rsi.getValue(index).doubleValue();
        double diff = val - UPPER;
        return diff > 0 ? diff : 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_14_tail_up",
                "RSI(14) Upper Tail",
                "momentum",
                "max(RSI(14) - 70, 0)",
                "Excesso do RSI(14) acima do limiar de overbought (70). Captura a intensidade da condicao de sobrecompra.",
                "0-30",
                "RSI(14) = 85 -> tail_up = 15.0"
        );
    }
}
