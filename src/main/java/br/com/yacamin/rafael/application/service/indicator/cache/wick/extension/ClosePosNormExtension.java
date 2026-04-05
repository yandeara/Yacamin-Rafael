package br.com.yacamin.rafael.application.service.indicator.cache.wick.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Close position normalizada = (close - low) / (high - low).
 * Retorna 0.5 se range < EPS.
 */
public class ClosePosNormExtension extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    public ClosePosNormExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        double range = high - low;

        if (range < EPS) {
            return getBarSeries().numFactory().numOf(0.5);
        }

        return getBarSeries().numFactory().numOf((close - low) / range);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
