package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension.AmihudExtension;

import org.springframework.stereotype.Component;

@Component
public class AmihudAccW5Calc implements DescribableCalc {

    public static double calculate(AmihudExtension amihud, int index) {
        double current = amihud.getValue(index).doubleValue();
        double lagged = amihud.getValue(index - 5).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_amihud_acc_w5",
                "Amihud Acceleration W5",
                "microstructure",
                "(amihud[t] - amihud[t-5]) / amihud[t-5]",
                "Aceleracao percentual do Amihud em 5 barras. " +
                "Complementa o W4 com uma janela ligeiramente maior, reduzindo ruido no calculo de momentum de iliquidez.",
                "unbounded",
                "amihud[t]=0.00002, amihud[t-5]=0.00004 -> (0.00002-0.00004)/0.00004 = -0.5"
        );
    }
}
