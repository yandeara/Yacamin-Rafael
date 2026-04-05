package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodySignPersistenceExtension;

import org.springframework.stereotype.Component;

@Component
public class BodySignPrstW20Calc implements DescribableCalc {

    public static double calculate(BodySignPersistenceExtension persistence, int index) {
        return persistence.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_body_sign_prst_w20",
                "Body Sign Persistence W20",
                "microstructure",
                "count(body > 0, 20) / 20",
                "Persistencia direcional dos ultimos 20 candles. " +
                "Valores altos indicam sequencia de candles bullish, baixos indicam bearish.",
                "0-1",
                "15 de 20 candles bullish -> 15/20 = 0.75"
        );
    }
}
