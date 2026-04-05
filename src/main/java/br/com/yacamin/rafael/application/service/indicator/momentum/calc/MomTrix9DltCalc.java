package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TrixExtension;

import org.springframework.stereotype.Component;

@Component
public class MomTrix9DltCalc implements DescribableCalc {

    public static double calculate(TrixExtension trix, int index) {
        return trix.getValue(index).doubleValue() - trix.getValue(index - 1).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_trix_9_dlt",
                "TRIX(9) Delta",
                "momentum",
                "TRIX(9)[t] - TRIX(9)[t-1]",
                "Variacao do TRIX(9) entre o candle atual e o anterior. Indica aceleracao do momentum curto.",
                "unbounded",
                "TRIX(9) subiu de 0.01 para 0.02 -> dlt = 0.01"
        );
    }
}
