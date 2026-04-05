package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.hasbrouck.extension.HasbrouckSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class HasbSlpW16Calc implements DescribableCalc {

    public static double calculate(HasbrouckSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_hasb_lambda_w16_slp_w20",
                "Hasbrouck Slope W16",
                "microstructure",
                "slope(hasbrouck_lambda(w=16), 20)",
                "Inclinacao (slope) do lambda de Hasbrouck (janela 16) em 20 barras. " +
                "Valores positivos indicam impacto de preco crescente, negativos indicam melhora de liquidez.",
                "R (qualquer valor real)",
                "slope(20)=0.000002 -> 0.000002"
        );
    }
}
