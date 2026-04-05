package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension.AmihudExtension;

import org.springframework.stereotype.Component;

@Component
public class AmihudAccW10Calc implements DescribableCalc {

    public static double calculate(AmihudExtension amihud, int index) {
        double current = amihud.getValue(index).doubleValue();
        double lagged = amihud.getValue(index - 10).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_amihud_acc_w10",
                "Amihud Acceleration W10",
                "microstructure",
                "(amihud[t] - amihud[t-10]) / amihud[t-10]",
                "Aceleracao percentual do Amihud em 10 barras. " +
                "Captura mudancas de liquidez em escala intermediaria, util para identificar transicoes de regime.",
                "unbounded",
                "amihud[t]=0.00001, amihud[t-10]=0.00001 -> (0.00001-0.00001)/0.00001 = 0.0"
        );
    }
}
