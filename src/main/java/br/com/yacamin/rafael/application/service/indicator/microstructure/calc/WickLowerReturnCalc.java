package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class WickLowerReturnCalc implements DescribableCalc {

    public static double calculate(BarSeries series, int index) {
        if (index <= 0) return 0;
        double low = series.getBar(index).getLowPrice().doubleValue();
        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < 1e-12) return 0;
        return (low - prevClose) / prevClose;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_lower_wick_return",
                "Lower Wick Return",
                "microstructure",
                "(low - close[t-1]) / close[t-1]",
                "Retorno da minima relativo ao close anterior. " +
                "Mede o alcance maximo de queda do candle como percentual.",
                "unbounded",
                "low=98, close[t-1]=100 -> (98-100)/100 = -0.02"
        );
    }
}
