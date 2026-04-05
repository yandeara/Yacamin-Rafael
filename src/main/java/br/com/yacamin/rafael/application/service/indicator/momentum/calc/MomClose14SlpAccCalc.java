package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;

import org.springframework.stereotype.Component;

@Component
public class MomClose14SlpAccCalc implements DescribableCalc {

    public static double calculate(DifferenceIndicator slopeAcc, int index) {
        return slopeAcc.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_close_14_slp_acc",
                "Close Slope 14 Acceleration",
                "momentum",
                "slope(close,14)[t] - slope(close,14)[t-1]",
                "Aceleracao do slope do close na janela de 14 periodos. Diferenca entre slope atual e anterior.",
                "R",
                "slope[t]=5.0, slope[t-1]=4.0 -> acc=1.0"
        );
    }
}
