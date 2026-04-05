package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyAbsExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class BodyShockAtrnCalc implements DescribableCalc {

    public static double calculate(BodyAbsExtension bodyAbs, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < 1e-12) return 0;
        return bodyAbs.getValue(index).doubleValue() / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_body_shock_atrn",
                "Body Shock ATRn",
                "microstructure",
                "|body| / ATR(14)",
                "Choque do corpo normalizado pelo ATR. " +
                "Valores > 1 indicam candle excepcional para o regime de volatilidade atual.",
                "0+",
                "|body|=150, ATR(14)=100 -> 150/100 = 1.5"
        );
    }
}
