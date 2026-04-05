package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi14AccCalc implements DescribableCalc {

    private static final int DIST = 2;

    public static double calculate(RSIIndicator rsi, int index) {
        double curr  = rsi.getValue(index).doubleValue();
        double prev  = rsi.getValue(index - DIST).doubleValue();
        double prev2 = rsi.getValue(index - 2 * DIST).doubleValue();
        double v1 = curr - prev;
        double v2 = prev - prev2;
        return v1 - v2;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_14_acc",
                "RSI(14) Acceleration",
                "momentum",
                "(RSI[t]-RSI[t-2]) - (RSI[t-2]-RSI[t-4])",
                "Aceleracao do RSI(14) — segunda diferenca com distancia 2. Detecta mudancas na taxa de variacao do momentum: valores positivos indicam aceleracao de alta, negativos desaceleracao.",
                "unbounded",
                "velocity subiu de 3 para 5 -> acceleration = 2.0"
        );
    }
}
