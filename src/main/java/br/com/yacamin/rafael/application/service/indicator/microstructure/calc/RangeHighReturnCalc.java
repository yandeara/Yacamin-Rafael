package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class RangeHighReturnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        if (index <= 0) return 0;
        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0;
        double high = series.getBar(index).getHighPrice().doubleValue();
        return (high - prevClose) / prevClose;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_high_return",
                "High Return",
                "microstructure",
                "(high - close[t-1]) / close[t-1]",
                "Retorno do high em relacao ao close anterior. " +
                "Mede a extensao maxima de alta da barra; util para estimar potencial de take-profit.",
                "unbounded",
                "high=40200, close[t-1]=40000 -> (40200-40000)/40000 = 0.005"
        );
    }
}
