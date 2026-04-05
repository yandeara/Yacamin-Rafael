package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TsiExtension;
import org.springframework.stereotype.Component;

@Component
public class MomTsi2513Sig7Calc implements DescribableCalc {

    public static double calculate(TsiExtension tsi, int index, int sigPeriod) {
        if (sigPeriod <= 1) return tsi.getValue(index).doubleValue();
        double alpha = 2.0 / (sigPeriod + 1.0);
        int start = Math.max(0, index - sigPeriod + 1);
        double ema = tsi.getValue(start).doubleValue();
        for (int i = start + 1; i <= index; i++) {
            ema = alpha * tsi.getValue(i).doubleValue() + (1.0 - alpha) * ema;
        }
        return ema;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_tsi_25_13_sig_7",
                "TSI 25/13 Signal EMA7",
                "momentum",
                "EMA(TSI(25,13), 7)",
                "Linha de sinal do TSI 25/13 calculada como EMA de 7 periodos. Cruzamentos com TSI geram sinais.",
                "-100 a 100",
                "TSI suavizado -> ~12"
        );
    }
}
