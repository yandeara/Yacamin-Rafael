package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.extension.VwapExtension;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class PosCloseVwapAtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(VwapExtension vwap, BarSeries series, ATRIndicator atr, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double vwapVal = vwap.getValue(index).doubleValue();
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < EPS) return 0;
        return (close - vwapVal) / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_close_vwap_atrn",
                "Close-VWAP ATR Normalizado",
                "microstructure",
                "(close - VWAP) / ATR",
                "Desvio do fechamento em relacao ao VWAP normalizado pelo ATR. " +
                "Mede o deslocamento do preco relativo ao fair value em unidades de volatilidade.",
                "unbounded (tipicamente -2 a 2)",
                "close=101, VWAP=100.5, ATR=2.0 -> (101-100.5)/2.0 = 0.25"
        );
    }
}
