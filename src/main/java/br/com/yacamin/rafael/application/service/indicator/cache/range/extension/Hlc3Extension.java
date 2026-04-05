package br.com.yacamin.rafael.application.service.indicator.cache.range.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * HLC3 (Typical Price) = (high + low + close) / 3
 */
public class Hlc3Extension extends CachedIndicator<Num> {

    public Hlc3Extension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);
        double hlc3 = (bar.getHighPrice().doubleValue()
                + bar.getLowPrice().doubleValue()
                + bar.getClosePrice().doubleValue()) / 3.0;
        return getBarSeries().numFactory().numOf(hlc3);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
