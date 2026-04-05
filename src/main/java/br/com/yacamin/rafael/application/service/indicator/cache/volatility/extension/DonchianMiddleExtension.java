package br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Donchian Middle = (upper + lower) / 2
 */
public class DonchianMiddleExtension extends CachedIndicator<Num> {

    private final DonchianUpperExtension upper;
    private final DonchianLowerExtension lower;

    public DonchianMiddleExtension(DonchianUpperExtension upper, DonchianLowerExtension lower) {
        super(upper.getBarSeries());
        this.upper = upper;
        this.lower = lower;
    }

    @Override
    protected Num calculate(int index) {
        return upper.getValue(index).plus(lower.getValue(index))
                .dividedBy(getBarSeries().numFactory().numOf(2));
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
