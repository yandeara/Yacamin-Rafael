package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class RangeLowReturnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        if (index <= 0) return 0;
        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0;
        double low = series.getBar(index).getLowPrice().doubleValue();
        return (low - prevClose) / prevClose;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_low_return",
                "Low Return",
                "microstructure",
                "(low - close[t-1]) / close[t-1]",
                "Retorno do low em relacao ao close anterior. " +
                "Mede a extensao maxima de queda da barra; util para estimar risco de stop-loss.",
                "unbounded",
                "low=39800, close[t-1]=40000 -> (39800-40000)/40000 = -0.005"
        );
    }
}
