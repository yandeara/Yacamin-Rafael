package br.com.yacamin.rafael.application.service.indicator.cache.wick.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Wick imbalance = (upper - lower) / (high - low).
 * Normalizado pelo range do candle, nao pela soma dos wicks.
 * Retorna 0 se range < EPS.
 */
public class WickImbalanceExtension extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final UpperWickExtension upper;
    private final LowerWickExtension lower;

    public WickImbalanceExtension(UpperWickExtension upper, LowerWickExtension lower) {
        super(upper.getBarSeries());
        this.upper = upper;
        this.lower = lower;
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);
        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        if (!Double.isFinite(range) || range < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        double u = upper.getValue(index).doubleValue();
        double l = lower.getValue(index).doubleValue();
        double imb = (u - l) / (range + EPS);
        if (!Double.isFinite(imb)) return getBarSeries().numFactory().numOf(0);

        return getBarSeries().numFactory().numOf(imb);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
