package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi7TailDwCalc implements DescribableCalc {

    private static final double LOWER = 30.0;

    public static double calculate(RSIIndicator rsi, int index) {
        double val = rsi.getValue(index).doubleValue();
        double diff = LOWER - val;
        return diff > 0 ? diff : 0.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_7_tail_dw",
                "RSI(7) Lower Tail",
                "momentum",
                "max(30 - RSI(7), 0)",
                "Excesso do RSI(7) abaixo do limiar de oversold (30). Captura a intensidade da condicao de sobrevenda.",
                "0-30",
                "RSI(7) = 15 -> tail_dw = 15.0"
        );
    }
}
