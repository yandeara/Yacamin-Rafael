package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;

import org.springframework.stereotype.Component;

@Component
public class MomClose8SlpAccCalc implements DescribableCalc {

    public static double calculate(DifferenceIndicator slopeAcc, int index) {
        return slopeAcc.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_close_8_slp_acc",
                "Close Slope 8 Acceleration",
                "momentum",
                "slope(close,8)[t] - slope(close,8)[t-1]",
                "Aceleracao do slope do close na janela de 8 periodos. Diferenca entre slope atual e anterior.",
                "R",
                "slope[t]=3.0, slope[t-1]=2.5 -> acc=0.5"
        );
    }
}
