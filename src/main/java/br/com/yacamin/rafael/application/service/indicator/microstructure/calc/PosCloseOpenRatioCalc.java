package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class PosCloseOpenRatioCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        double close = series.getBar(index).getClosePrice().doubleValue();
        double open = series.getBar(index).getOpenPrice().doubleValue();
        if (Math.abs(open) < EPS) return 0;
        return close / open;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_close_open_ratio",
                "Close/Open Ratio",
                "microstructure",
                "close / open",
                "Razao entre fechamento e abertura. Valores acima de 1 indicam candle bullish, " +
                "abaixo de 1 bearish. Mede a direcao e intensidade relativa do candle.",
                "0+ (tipicamente proximo de 1)",
                "close=101.0, open=100.0 -> 101.0 / 100.0 = 1.01"
        );
    }
}
