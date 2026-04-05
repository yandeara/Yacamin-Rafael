package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyEnergyExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class BodyEnergyAtrnCalc implements DescribableCalc {

    public static double calculate(BodyEnergyExtension energy, ATRIndicator atr, int index) {
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < 1e-12) return 0;
        return energy.getValue(index).doubleValue() / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_energy_atrn",
                "Candle Energy ATR-Normalized",
                "microstructure",
                "(|body| * range) / ATR(14)",
                "Energia do candle normalizada pelo ATR. " +
                "Permite comparar a energia de candles entre periodos com volatilidades distintas.",
                "0+",
                "energy=4.5, ATR(14)=3.0 -> 4.5 / 3.0 = 1.5"
        );
    }
}
