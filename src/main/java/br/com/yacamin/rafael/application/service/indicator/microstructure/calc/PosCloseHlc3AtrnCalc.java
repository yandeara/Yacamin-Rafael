package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class PosCloseHlc3AtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, ATRIndicator atr, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double hlc3 = (high + low + close) / 3.0;
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < EPS) return 0;
        return (close - hlc3) / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_close_hlc3_atrn",
                "Close-HLC3 ATR Normalizado",
                "microstructure",
                "(close - HLC3) / ATR",
                "Desvio do fechamento em relacao ao HLC3 normalizado pelo ATR. " +
                "Mede a intensidade do deslocamento do close em unidades de volatilidade.",
                "unbounded (tipicamente -1 a 1)",
                "close=101, HLC3=100.67, ATR=2.0 -> (101-100.67)/2.0 = 0.165"
        );
    }
}
