package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.TrueRangeExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class RangeTrAtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(TrueRangeExtension tr, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < EPS) return 0;
        return tr.getValue(index).doubleValue() / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_tr_atrn",
                "True Range ATR-Normalized",
                "microstructure",
                "TR / ATR",
                "True Range normalizado pelo ATR. " +
                "Valores acima de 1.0 indicam barra com TR acima da media (possivel gap ou choque); util para filtrar entradas em volatilidade extrema.",
                "0+",
                "tr=400, atr=200 -> 400/200 = 2.0"
        );
    }
}
