package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class LowerWickIndicator extends CachedIndicator<Num> {

    public LowerWickIndicator(org.ta4j.core.BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);

        double l = bar.getLowPrice().doubleValue();
        double o = bar.getOpenPrice().doubleValue();
        double c = bar.getClosePrice().doubleValue();

        double lower = Math.min(o, c) - l;
        if (!Double.isFinite(lower) || lower < 0) lower = 0.0;

        return getBarSeries().numFactory().numOf(lower);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
