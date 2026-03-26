package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class RangeIndicator extends CachedIndicator<Num> {

    public RangeIndicator(org.ta4j.core.BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);
        double r = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        if (!Double.isFinite(r) || r < 0) r = 0.0;
        return getBarSeries().numFactory().numOf(r);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
