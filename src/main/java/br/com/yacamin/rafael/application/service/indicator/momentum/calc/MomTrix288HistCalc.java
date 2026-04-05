package br.com.yacamin.rafael.application.service.indicator.momentum.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension.TrixExtension;

import org.springframework.stereotype.Component;

@Component
public class MomTrix288HistCalc implements DescribableCalc {

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
                "mom_trix_288_hist",
                "TRIX(288) Histogram",
                "momentum",
                "TRIX(288) - EMA(9) of TRIX(288)",
                "Histograma do TRIX(288): diferenca entre TRIX e sua linha de sinal. Positivo=bullish, negativo=bearish.",
                "unbounded",
                "TRIX(288)=0.001, signal=0.003 -> hist=-0.002 (bearish)"
        );
    }
}
