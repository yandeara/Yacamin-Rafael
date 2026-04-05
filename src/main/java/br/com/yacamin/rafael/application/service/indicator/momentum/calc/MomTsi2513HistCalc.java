package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TsiExtension;
import org.springframework.stereotype.Component;

@Component
public class MomTsi2513HistCalc implements DescribableCalc {

    public static double calculate(TsiExtension tsi, int index, int sigPeriod) {
        double raw = tsi.getValue(index).doubleValue();
        double sig = MomTsi2513Sig7Calc.calculate(tsi, index, sigPeriod);
        return raw - sig;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_tsi_25_13_hist",
                "TSI 25/13 Histogram",
                "momentum",
                "TSI(25,13) - Signal(7)",
                "Histograma do TSI (diferenca entre TSI e sinal). Positivo indica momentum bullish acelerando.",
                "unbounded",
                "TSI=15, Signal=12 -> 3.0"
        );
    }
}
