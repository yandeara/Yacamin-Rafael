package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension.AmihudExtension;

import org.springframework.stereotype.Component;

@Component
public class AmihudAccW16Calc implements DescribableCalc {

    public static double calculate(AmihudExtension amihud, int index) {
        double current = amihud.getValue(index).doubleValue();
        double lagged = amihud.getValue(index - 16).doubleValue();
        return current - lagged;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_amihud_acc_w16",
                "Amihud Acceleration W16",
                "microstructure",
                "(amihud[t] - amihud[t-16]) / amihud[t-16]",
                "Aceleracao percentual do Amihud em 16 barras. " +
                "Janela mais longa para capturar tendencias de aceleracao/desaceleracao de iliquidez em prazo maior.",
                "unbounded",
                "amihud[t]=0.00005, amihud[t-16]=0.00002 -> (0.00005-0.00002)/0.00002 = 1.5"
        );
    }
}
