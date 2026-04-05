package br.com.yacamin.rafael.application.service.indicator.cache.wick.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Upper wick = high - max(open, close).
 */
public class UpperWickExtension extends CachedIndicator<Num> {

    public UpperWickExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        double high = bar.getHighPrice().doubleValue();
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        return getBarSeries().numFactory().numOf(high - Math.max(open, close));
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
