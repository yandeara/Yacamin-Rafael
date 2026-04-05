package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TsiExtension;
import org.springframework.stereotype.Component;

@Component
public class MomTsi288144HistCalc implements DescribableCalc {

    public static double calculate(TsiExtension tsi, int index, int sigPeriod) {
        double raw = tsi.getValue(index).doubleValue();
        double sig = MomTsi288144Sig7Calc.calculate(tsi, index, sigPeriod);
        return raw - sig;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_tsi_288_144_hist",
                "TSI 288/144 Histogram",
                "momentum",
                "TSI(288,144) - Signal(7)",
                "Histograma do TSI 288/144.",
                "unbounded",
                "TSI=15, Signal=12 -> 3.0"
        );
    }
}
