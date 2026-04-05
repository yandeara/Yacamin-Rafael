package br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Burst Strength = max(|ret1|) over a rolling window.
 * Takes a CloseReturnExtension with window=1 as source.
 */
public class BurstStrengthExtension extends CachedIndicator<Num> {

    private final CloseReturnExtension ret1;
    private final int window;

    public BurstStrengthExtension(CloseReturnExtension ret1, int window) {
        super(ret1.getBarSeries());
        this.ret1 = ret1;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        int from = Math.max(0, index - window + 1);
        double maxAbs = 0.0;

        for (int i = from; i <= index; i++) {
            double abs = Math.abs(ret1.getValue(i).doubleValue());
            if (abs > maxAbs) {
                maxAbs = abs;
            }
        }

        return getBarSeries().numFactory().numOf(maxAbs);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
