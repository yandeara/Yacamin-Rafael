package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.BurstStrengthExtension;

import org.springframework.stereotype.Component;

@Component
public class MomBurst10Calc implements DescribableCalc {

    public static double calculate(BurstStrengthExtension burstStrength, int index) {
        return burstStrength.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_burst_10",
                "Burst Strength 10",
                "momentum",
                "max(|ret_1|) over 10 bars",
                "Forca maxima de impulso nos ultimos 10 periodos. " +
                "Detecta picos de volatilidade direcional recente.",
                "[0, +inf)",
                "max(|ret_1|) over 10 bars = 0.035"
        );
    }
}
