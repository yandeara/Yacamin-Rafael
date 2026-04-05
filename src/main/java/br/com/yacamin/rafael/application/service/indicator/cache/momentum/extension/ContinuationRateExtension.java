package br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Continuation Rate = fraction of bars in the window whose return sign
 * matches the dominant direction (sign of the last bar's return).
 * Takes a CloseReturnExtension with window=1 as source.
 */
public class ContinuationRateExtension extends CachedIndicator<Num> {

    private final CloseReturnExtension ret1;
    private final int window;

    public ContinuationRateExtension(CloseReturnExtension ret1, int window) {
        super(ret1.getBarSeries());
        this.ret1 = ret1;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        int from = Math.max(0, index - window + 1);
        int len = index - from + 1;

        double lastRet = ret1.getValue(index).doubleValue();
        int currSign = sign(lastRet);

        int count = 0;
        for (int i = from; i <= index; i++) {
            if (sign(ret1.getValue(i).doubleValue()) == currSign) {
                count++;
            }
        }

        return getBarSeries().numFactory().numOf((double) count / (double) len);
    }

    private int sign(double v) {
        return v > 0.0 ? 1 : (v < 0.0 ? -1 : 0);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
