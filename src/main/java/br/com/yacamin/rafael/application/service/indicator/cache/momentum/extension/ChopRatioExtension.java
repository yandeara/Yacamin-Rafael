package br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Chop Ratio = (number of sign changes) / (n - 1) over a rolling window.
 * Takes a CloseReturnExtension with window=1 as source.
 */
public class ChopRatioExtension extends CachedIndicator<Num> {

    private final CloseReturnExtension ret1;
    private final int window;

    public ChopRatioExtension(CloseReturnExtension ret1, int window) {
        super(ret1.getBarSeries());
        this.ret1 = ret1;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        int from = Math.max(0, index - window + 1);
        int n = index - from + 1;

        if (n < 2) {
            return getBarSeries().numFactory().numOf(0);
        }

        int changes = 0;
        int prev = sign(ret1.getValue(from).doubleValue());

        for (int i = from + 1; i <= index; i++) {
            int s = sign(ret1.getValue(i).doubleValue());
            if (s != 0 && prev != 0 && s != prev) {
                changes++;
            }
            if (s != 0) {
                prev = s;
            }
        }

        return getBarSeries().numFactory().numOf(changes / (double) (n - 1));
    }

    private int sign(double v) {
        return v > 0.0 ? 1 : (v < 0.0 ? -1 : 0);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
