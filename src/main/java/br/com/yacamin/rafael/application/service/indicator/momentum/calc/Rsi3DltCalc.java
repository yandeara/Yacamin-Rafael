package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi3DltCalc implements DescribableCalc {

    public static double calculate(RSIIndicator rsi, int index) {
        return rsi.getValue(index).doubleValue() - rsi.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_3_dlt",
                "RSI(3) Delta",
                "momentum",
                "RSI(3)[t] - RSI(3)[t-1]",
                "Variacao absoluta do RSI(3) entre o candle atual e o anterior. Valores positivos indicam aceleracao de alta, negativos indicam aceleracao de baixa.",
                "unbounded (tipicamente -20 a 20)",
                "RSI(3) subiu de 55 para 60 -> delta = 5.0"
        );
    }
}
