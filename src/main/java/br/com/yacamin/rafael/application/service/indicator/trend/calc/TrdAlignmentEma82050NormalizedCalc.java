package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Component
public class TrdAlignmentEma82050NormalizedCalc implements DescribableCalc {

    public static double calculate(EMAIndicator e8, EMAIndicator e20, EMAIndicator e50, int index) {
        double f = e8.getValue(index).doubleValue();
        double m = e20.getValue(index).doubleValue();
        double s = e50.getValue(index).doubleValue();
        int score = 0;
        if (f > m) score++;
        if (m > s) score++;
        if (f > s) score++;
        return score / 3.0;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_alignment_ema_8_20_50_normalized", "EMA Alignment Normalized", "trend", "alignmentScore / 3",
                "Score de alinhamento normalizado para [0,1]. 1.0 = alinhamento bullish perfeito.",
                "0-1", "score=3 -> 1.0"
        );
    }
}
