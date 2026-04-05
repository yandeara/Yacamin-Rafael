package br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Bollinger Width = (upper - lower) / middle
 */
public class BollingerWidthExtension extends CachedIndicator<Num> {

    private final Indicator<Num> upper;
    private final Indicator<Num> lower;
    private final Indicator<Num> middle;

    public BollingerWidthExtension(Indicator<Num> upper, Indicator<Num> lower, Indicator<Num> middle) {
        super(upper.getBarSeries());
        this.upper = upper;
        this.lower = lower;
        this.middle = middle;
    }

    @Override
    protected Num calculate(int index) {
        Num width = upper.getValue(index).minus(lower.getValue(index));
        return width.abs();
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
