package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;

@Component
public class TrdDiDiff14Calc implements DescribableCalc {

    public static double calculate(PlusDIIndicator pdi, MinusDIIndicator mdi, int index) {
        return pdi.getValue(index).doubleValue() - mdi.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_di_diff_14", "DI Diff 14", "trend", "+DI(14) - -DI(14)",
                "Diferenca entre Plus DI e Minus DI. Positivo = pressao compradora dominante, negativo = vendedora.",
                "unbounded", "+DI=30, -DI=15 -> diDiff=15"
        );
    }
}
