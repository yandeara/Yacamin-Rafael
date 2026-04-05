package br.com.yacamin.rafael.application.service.indicator.cache.wick.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Lower wick = min(open, close) - low.
 */
public class LowerWickExtension extends CachedIndicator<Num> {

    public LowerWickExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        double low = bar.getLowPrice().doubleValue();
        double open = bar.getOpenPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();
        return getBarSeries().numFactory().numOf(Math.min(open, close) - low);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
