package br.com.yacamin.rafael.application.service.indicator.cache.range.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Range = high - low
 */
public class RangeExtension extends CachedIndicator<Num> {

    public RangeExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);
        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        return getBarSeries().numFactory().numOf(range);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
