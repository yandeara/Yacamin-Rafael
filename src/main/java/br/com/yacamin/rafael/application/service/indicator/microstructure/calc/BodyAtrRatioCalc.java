package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyAbsExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class BodyAtrRatioCalc implements DescribableCalc {

    public static double calculate(BodyAbsExtension bodyAbs, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < 1e-12) return 0;
        return bodyAbs.getValue(index).doubleValue() / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_body_atr_ratio",
                "Body ATR Ratio",
                "microstructure",
                "|body| / ATR(14)",
                "Tamanho do corpo normalizado pela volatilidade ATR. " +
                "Valores altos indicam candle com movimento forte relativo ao regime atual.",
                "0+",
                "|body|=1.5, ATR(14)=3.0 -> 1.5 / 3.0 = 0.5"
        );
    }
}
