package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class RangeHeadroomAtrCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RangeExtension range, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < EPS) return 0;
        return (atrVal - range.getValue(index).doubleValue()) / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_headroom_atr",
                "Range Headroom ATR",
                "microstructure",
                "(ATR - range) / ATR",
                "Espaco residual entre range atual e ATR. " +
                "Valores positivos indicam que a barra nao usou toda a volatilidade esperada (potencial de continuacao); negativos indicam overshooting.",
                "unbounded",
                "atr=200, range=150 -> (200-150)/200 = 0.25"
        );
    }
}
