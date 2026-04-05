package br.com.yacamin.rafael.application.service.indicator.trend.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Component
public class TrdAlignmentEma82050ScoreCalc implements DescribableCalc {

    public static double calculate(EMAIndicator e8, EMAIndicator e20, EMAIndicator e50, int index) {
        double f = e8.getValue(index).doubleValue();
        double m = e20.getValue(index).doubleValue();
        double s = e50.getValue(index).doubleValue();
        int score = 0;
        if (f > m) score++;
        if (m > s) score++;
        if (f > s) score++;
        return (double) score;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "trd_alignment_ema_8_20_50_score", "EMA Alignment Score", "trend", "count(EMA8>EMA20, EMA20>EMA50, EMA8>EMA50)",
                "Score de alinhamento das EMAs 8/20/50. Score 3 = alinhamento bullish perfeito, 0 = bearish perfeito.",
                "0-3", "EMA8>EMA20>EMA50 -> score=3"
        );
    }
}
