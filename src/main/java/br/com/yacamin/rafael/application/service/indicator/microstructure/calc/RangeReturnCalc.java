package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class RangeReturnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RangeExtension range, BarSeries series, int index) {
        if (index <= 0) return 0;
        double prevClose = series.getBar(index - 1).getClosePrice().doubleValue();
        if (Math.abs(prevClose) < EPS) return 0;
        return range.getValue(index).doubleValue() / prevClose;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_return",
                "Range Return",
                "microstructure",
                "range / close[t-1]",
                "Range normalizado pelo close anterior. " +
                "Mede a amplitude da barra como percentual do preco, facilitando comparacao entre niveis de preco distintos.",
                "0+",
                "range=200, close[t-1]=40000 -> 200/40000 = 0.005"
        );
    }
}
