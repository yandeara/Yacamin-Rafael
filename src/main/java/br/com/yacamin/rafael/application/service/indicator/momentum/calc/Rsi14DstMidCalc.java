package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi14DstMidCalc implements DescribableCalc {

    public static double calculate(RSIIndicator rsi, int index) {
        return Math.abs(rsi.getValue(index).doubleValue() - 50.0);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_14_dst_mid",
                "RSI(14) Distance from 50",
                "momentum",
                "|RSI(14) - 50|",
                "Distancia absoluta do RSI(14) em relacao ao ponto neutro 50. Valores altos indicam momentum extremo (overbought ou oversold).",
                "0-50",
                "RSI(14) = 75 -> dst_mid = 25.0"
        );
    }
}
