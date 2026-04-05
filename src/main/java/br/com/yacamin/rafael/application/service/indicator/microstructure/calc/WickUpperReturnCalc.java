package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickUpperReturnCalc implements DescribableCalc {

    public static double calculate(BarSeries series, int index) {
        if (index <= 0) return 0;
        double high = series.getBar(index).getHighPrice().doubleValue();
        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < 1e-12) return 0;
        return (high - prevClose) / prevClose;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_upper_wick_return",
                "Upper Wick Return",
                "microstructure",
                "(high - close[t-1]) / close[t-1]",
                "Retorno da maxima relativo ao close anterior. " +
                "Mede o alcance maximo de alta do candle como percentual.",
                "unbounded",
                "high=102, close[t-1]=100 -> (102-100)/100 = 0.02"
        );
    }
}
