package br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Decay Rate = OLS slope of |ret1| over a rolling window.
 * Takes a CloseReturnExtension with window=1 as source.
 */
public class DecayRateExtension extends CachedIndicator<Num> {

    private final CloseReturnExtension ret1;
    private final int window;

    public DecayRateExtension(CloseReturnExtension ret1, int window) {
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

        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = Math.abs(ret1.getValue(from + i).doubleValue());

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);
        if (Math.abs(denom) < 1e-18) {
            return getBarSeries().numFactory().numOf(0);
        }

        double slope = (n * sumXY - sumX * sumY) / denom;
        return getBarSeries().numFactory().numOf(slope);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
