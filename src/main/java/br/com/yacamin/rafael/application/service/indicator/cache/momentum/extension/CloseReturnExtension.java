package br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

/**
 * Close Return = (close[t] / close[t - window]) - 1
 */
public class CloseReturnExtension extends CachedIndicator<Num> {

    private final ClosePriceIndicator close;
    private final int window;

    public CloseReturnExtension(ClosePriceIndicator close, int window) {
        super(close.getBarSeries());
        this.close = close;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (index < window) {
            return getBarSeries().numFactory().numOf(0);
        }

        double current = close.getValue(index).doubleValue();
        double previous = close.getValue(index - window).doubleValue();

        if (Math.abs(previous) < 1e-12) {
            return getBarSeries().numFactory().numOf(0);
        }

        return getBarSeries().numFactory().numOf((current / previous) - 1.0);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
