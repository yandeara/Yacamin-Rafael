package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension.AmihudExtension;

import org.springframework.stereotype.Component;

@Component
public class AmihudAccW4Calc implements DescribableCalc {

    public static double calculate(AmihudExtension amihud, int index) {
        double current = amihud.getValue(index).doubleValue();
        double lagged = amihud.getValue(index - 4).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_amihud_acc_w4",
                "Amihud Acceleration W4",
                "microstructure",
                "(amihud[t] - amihud[t-4]) / amihud[t-4]",
                "Aceleracao percentual do Amihud em 4 barras. " +
                "Mede a taxa de variacao da iliquidez no curtissimo prazo, detectando mudancas abruptas de liquidez.",
                "unbounded",
                "amihud[t]=0.00003, amihud[t-4]=0.00001 -> (0.00003-0.00001)/0.00001 = 2.0"
        );
    }
}
