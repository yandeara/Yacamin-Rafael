package br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Impulse = sum of ret1 over a rolling window.
 * Equivalent to sum(sign(ret_i) * |ret_i|) = sum(ret_i).
 * Takes a CloseReturnExtension with window=1 as source.
 */
public class ImpulseExtension extends CachedIndicator<Num> {

    private final CloseReturnExtension ret1;
    private final int window;

    public ImpulseExtension(CloseReturnExtension ret1, int window) {
        super(ret1.getBarSeries());
        this.ret1 = ret1;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        int from = Math.max(0, index - window + 1);
        double sum = 0.0;

        for (int i = from; i <= index; i++) {
            sum += ret1.getValue(i).doubleValue();
        }

        return getBarSeries().numFactory().numOf(sum);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
