package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.body.extension.BodyRunLenExtension;

import org.springframework.stereotype.Component;

@Component
public class BodyRunLenCalc implements DescribableCalc {

    public static double calculate(BodyRunLenExtension runLen, int index) {
        return runLen.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_body_run_len",
                "Body Run Length",
                "microstructure",
                "consecutive_same_sign_count (signed)",
                "Comprimento da sequencia atual de candles com mesma direcao. " +
                "Positivo=sequencia bullish, negativo=bearish. Detecta momentum direcional.",
                "unbounded",
                "5 candles bullish consecutivos -> 5, 3 bearish consecutivos -> -3"
        );
    }
}
