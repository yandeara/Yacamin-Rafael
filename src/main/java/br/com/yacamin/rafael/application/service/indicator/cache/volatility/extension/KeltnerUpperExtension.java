package br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension;

import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Keltner Upper = middle + (ATR * multiplier)
 */
public class KeltnerUpperExtension extends CachedIndicator<Num> {

    private final KeltnerMiddleExtension middle;
    private final ATRIndicator atr;
    private final Num multiplier;

    public KeltnerUpperExtension(KeltnerMiddleExtension middle, ATRIndicator atr, double multiplier) {
        super(middle.getBarSeries());
        this.middle = middle;
        this.atr = atr;
        this.multiplier = getBarSeries().numFactory().numOf(multiplier);
    }

    @Override
    protected Num calculate(int index) {
        return middle.getValue(index).plus(atr.getValue(index).multipliedBy(multiplier));
    }

    @Override
    public int getCountOfUnstableBars() {
        return Math.max(middle.getCountOfUnstableBars(), atr.getCountOfUnstableBars());
    }
}
