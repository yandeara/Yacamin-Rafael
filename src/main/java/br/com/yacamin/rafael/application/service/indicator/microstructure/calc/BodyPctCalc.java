package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyAbsExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class BodyPctCalc implements DescribableCalc {

    public static double calculate(BodyAbsExtension bodyAbs, BarSeries series, int index) {
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double range = high - low;
        if (Math.abs(range) < 1e-12) return 0;
        return bodyAbs.getValue(index).doubleValue() / range;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_body_pct",
                "Body Pct",
                "microstructure",
                "|body| / range",
                "Percentual do range ocupado pelo corpo absoluto do candle. " +
                "Mede a dominancia do corpo sobre as sombras.",
                "0-1",
                "|close-open|=1.0, high=102, low=99 -> 1.0/3.0 = 0.333"
        );
    }
}
