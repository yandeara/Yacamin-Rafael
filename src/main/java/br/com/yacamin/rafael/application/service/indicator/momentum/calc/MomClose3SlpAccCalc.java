package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;

import org.springframework.stereotype.Component;

@Component
public class MomClose3SlpAccCalc implements DescribableCalc {

    public static double calculate(DifferenceIndicator slopeAcc, int index) {
        return slopeAcc.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_close_3_slp_acc",
                "Close Slope 3 Acceleration",
                "momentum",
                "slope(close,3)[t] - slope(close,3)[t-1]",
                "Aceleracao do slope do close na janela de 3 periodos. Diferenca entre slope atual e anterior.",
                "R",
                "slope[t]=2.0, slope[t-1]=1.5 -> acc=0.5"
        );
    }
}
