package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.RSIIndicator;

@Component
public class Rsi14AtrnCalc implements DescribableCalc {

    public static double calculate(RSIIndicator rsi, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (atrVal == 0.0) return 0.0;
        return rsi.getValue(index).doubleValue() / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_rsi_14_atrn",
                "RSI(14) ATR-Normalized",
                "momentum",
                "RSI(14) / ATR(14)",
                "RSI(14) normalizado pelo ATR(14). Ajusta o momentum pela volatilidade corrente, permitindo comparacoes entre periodos com volatilidades distintas.",
                "0+",
                "RSI(14)=65, ATR(14)=50.0 -> 65/50 = 1.3"
        );
    }
}
