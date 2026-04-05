package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;

@Component
public class PosCandleBalanceScoreCalc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(BarSeries series, int index) {
        double o = series.getBar(index).getOpenPrice().doubleValue();
        double c = series.getBar(index).getClosePrice().doubleValue();
        double h = series.getBar(index).getHighPrice().doubleValue();
        double l = series.getBar(index).getLowPrice().doubleValue();
        double range = h - l;
        if (range < EPS) return 0.5;
        double upperWick = h - Math.max(o, c);
        double lowerWick = Math.min(o, c) - l;
        double wickImb = Math.abs(upperWick - lowerWick) / range;
        double closePos = (c - l) / range;
        double posImb = Math.abs(closePos - 0.5) * 2.0;
        return 1.0 - 0.5 * (wickImb + posImb);
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_candle_balance_score",
                "Candle Balance Score",
                "microstructure",
                "1 - 0.5 * (wickImbalance + positionImbalance)",
                "Score de equilibrio do candle baseado na simetria dos pavios e posicao do fechamento. " +
                "Valores proximos de 1 indicam candle equilibrado (doji-like), proximos de 0 indicam dominio unilateral.",
                "0 to 1",
                "candle simetrico com close no centro -> wickImb=0, posImb=0 -> 1.0"
        );
    }
}
