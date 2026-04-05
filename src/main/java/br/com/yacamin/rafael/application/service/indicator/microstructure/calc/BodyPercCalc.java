package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class BodyPercCalc implements DescribableCalc {

    public static double calculate(BodyExtension body, BarSeries series, int index) {
        double open = series.getBar(index).getOpenPrice().doubleValue();
        if (Math.abs(open) < 1e-12) return 0;
        return body.getValue(index).doubleValue() / open;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_body_perc",
                "Body Perc",
                "microstructure",
                "body / open",
                "Retorno percentual do corpo relativo ao preco de abertura. " +
                "Mede o impacto proporcional do candle.",
                "unbounded",
                "close=101, open=100 -> (101-100)/100 = 0.01"
        );
    }
}
