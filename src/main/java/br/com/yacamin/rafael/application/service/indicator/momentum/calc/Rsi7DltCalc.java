package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi7DltCalc implements DescribableCalc {

    public static double calculate(RSIIndicator rsi, int index) {
        return rsi.getValue(index).doubleValue() - rsi.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_7_dlt",
                "RSI(7) Delta",
                "momentum",
                "RSI(7)[t] - RSI(7)[t-1]",
                "Variacao absoluta do RSI(7) entre o candle atual e o anterior. Valores positivos indicam aceleracao de alta, negativos indicam aceleracao de baixa.",
                "unbounded (tipicamente -20 a 20)",
                "RSI(7) subiu de 55 para 60 -> delta = 5.0"
        );
    }
}
