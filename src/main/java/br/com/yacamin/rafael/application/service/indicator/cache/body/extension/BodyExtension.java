package br.com.yacamin.rafael.application.service.indicator.cache.body.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Body = close - open (signed).
 */
public class BodyExtension extends CachedIndicator<Num> {

    public BodyExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        double close = bar.getClosePrice().doubleValue();
        double open = bar.getOpenPrice().doubleValue();
        return getBarSeries().numFactory().numOf(close - open);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
