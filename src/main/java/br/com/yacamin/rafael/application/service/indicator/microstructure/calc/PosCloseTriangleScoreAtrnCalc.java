package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Component
public class PosCloseTriangleScoreAtrnCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, ATRIndicator atr, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double high = series.getBar(index).getHighPrice().doubleValue();
        double low = series.getBar(index).getLowPrice().doubleValue();
        double mid = (high + low) / 2.0;
        double atrVal = atr.getValue(index).doubleValue();
        if (Math.abs(atrVal) < EPS) return 0;
        return Math.abs(close - mid) / atrVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_close_triangle_score_atrn",
                "Close Triangle Score ATR Normalizado",
                "microstructure",
                "|close - midpoint| / ATR",
                "Distancia do fechamento ao ponto medio do candle normalizada pelo ATR. " +
                "Valores altos indicam fechamento longe do centro em unidades de volatilidade, sugerindo momentum direcional.",
                "0+",
                "close=102, mid=101, ATR=2.0 -> |102-101|/2.0 = 0.5"
        );
    }
}
