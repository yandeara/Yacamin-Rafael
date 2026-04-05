package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Component
public class BodyReturnCalc implements DescribableCalc {

    public static double calculate(BodyExtension body, ClosePriceIndicator close, int index) {
        if (index <= 0) return 0;
        double prevClose = close.getValue(index - 1).doubleValue();
        if (Math.abs(prevClose) < 1e-12) return 0;
        return body.getValue(index).doubleValue() / prevClose;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_body_return",
                "Body Return",
                "microstructure",
                "body / close[t-1]",
                "Retorno do corpo normalizado pelo close anterior. " +
                "Captura a contribuicao do corpo para o retorno total.",
                "unbounded",
                "body=1.5, close[t-1]=100 -> 1.5/100 = 0.015"
        );
    }
}
