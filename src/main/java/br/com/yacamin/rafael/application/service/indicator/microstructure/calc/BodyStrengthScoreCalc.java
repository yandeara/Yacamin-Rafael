package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyRatioExtension;

import org.springframework.stereotype.Component;

@Component
public class BodyStrengthScoreCalc implements DescribableCalc {

    public static double calculate(BodyRatioExtension ratio, int index) {
        return Math.abs(ratio.getValue(index).doubleValue());
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_body_strength_score",
                "Body Strength Score",
                "microstructure",
                "|body / range|",
                "Score de forca do corpo do candle. " +
                "Identico ao strength, mantido por compatibilidade semantica.",
                "0-1",
                "close=101, open=100, high=102, low=99 -> |1/3| = 0.333"
        );
    }
}
