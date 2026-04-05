package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TsiExtension;
import org.springframework.stereotype.Component;

@Component
public class MomTsi4825HistCalc implements DescribableCalc {

    public static double calculate(TsiExtension tsi, int index, int sigPeriod) {
        double raw = tsi.getValue(index).doubleValue();
        double sig = MomTsi4825Sig7Calc.calculate(tsi, index, sigPeriod);
        return raw - sig;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_tsi_48_25_hist",
                "TSI 48/25 Histogram",
                "momentum",
                "TSI(48,25) - Signal(7)",
                "Histograma do TSI 48/25.",
                "unbounded",
                "TSI=15, Signal=12 -> 3.0"
        );
    }
}
