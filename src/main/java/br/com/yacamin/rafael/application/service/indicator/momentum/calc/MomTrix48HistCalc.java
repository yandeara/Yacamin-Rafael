package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TrixExtension;

import org.springframework.stereotype.Component;

@Component
public class MomTrix48HistCalc implements DescribableCalc {

    private static final int SIG_PERIOD = 9;

    public static double calculate(TrixExtension trix, int index) {
        double raw = trix.getValue(index).doubleValue();
        double sig = signalEma(trix, index, SIG_PERIOD);
        return raw - sig;
    }

    private static double signalEma(TrixExtension trix, int last, int sigPeriod) {
        if (sigPeriod <= 1) return trix.getValue(last).doubleValue();
        double alpha = 2.0 / (sigPeriod + 1.0);
        int start = Math.max(0, last - sigPeriod + 1);
        double ema = trix.getValue(start).doubleValue();
        for (int i = start + 1; i <= last; i++) {
            ema = alpha * trix.getValue(i).doubleValue() + (1.0 - alpha) * ema;
        }
        return ema;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mom_trix_48_hist",
                "TRIX(48) Histogram",
                "momentum",
                "TRIX(48) - EMA(9) of TRIX(48)",
                "Histograma do TRIX(48): diferenca entre TRIX e sua linha de sinal. Positivo=bullish, negativo=bearish.",
                "unbounded",
                "TRIX(48)=0.02, signal=0.025 -> hist=-0.005 (bearish)"
        );
    }
}
