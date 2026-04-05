package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.BurstStrengthExtension;

import org.springframework.stereotype.Component;

@Component
public class MomBurst16Calc implements DescribableCalc {

    public static double calculate(BurstStrengthExtension burstStrength, int index) {
        return burstStrength.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_burst_16",
                "Burst Strength 16",
                "momentum",
                "max(|ret_1|) over 16 bars",
                "Forca maxima de impulso nos ultimos 16 periodos. " +
                "Detecta picos de volatilidade direcional recente.",
                "[0, +inf)",
                "max(|ret_1|) over 16 bars = 0.035"
        );
    }
}
