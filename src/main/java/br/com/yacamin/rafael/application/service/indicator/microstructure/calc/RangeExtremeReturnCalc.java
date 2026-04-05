package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class RangeExtremeReturnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        if (index <= 0) return 0;
        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0;
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        return Math.max(Math.abs(high - prevClose), Math.abs(low - prevClose)) / prevClose;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_extreme_range_return",
                "Extreme Range Return",
                "microstructure",
                "max(|high-prevClose|, |low-prevClose|) / prevClose",
                "Maior extensao absoluta da barra em relacao ao close anterior. " +
                "Captura o movimento extremo da barra independente de direcao; util para deteccao de choque.",
                "0+",
                "high=40300, low=39800, prevClose=40000 -> max(300,200)/40000 = 0.0075"
        );
    }
}
