package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi2DltCalc implements DescribableCalc {

    public static double calculate(RSIIndicator rsi, int index) {
        return rsi.getValue(index).doubleValue() - rsi.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_2_dlt",
                "RSI(2) Delta",
                "momentum",
                "RSI(2)[t] - RSI(2)[t-1]",
                "Variacao absoluta do RSI(2) entre o candle atual e o anterior. Valores positivos indicam aceleracao de alta, negativos indicam aceleracao de baixa.",
                "unbounded (tipicamente -20 a 20)",
                "RSI(2) subiu de 55 para 60 -> delta = 5.0"
        );
    }
}
