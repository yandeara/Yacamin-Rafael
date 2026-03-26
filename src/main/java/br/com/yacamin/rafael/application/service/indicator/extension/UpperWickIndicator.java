package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class UpperWickIndicator extends CachedIndicator<Num> {

    public UpperWickIndicator(org.ta4j.core.BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);

        double h = bar.getHighPrice().doubleValue();
        double o = bar.getOpenPrice().doubleValue();
        double c = bar.getClosePrice().doubleValue();

        double upper = h - Math.max(o, c);
        if (!Double.isFinite(upper) || upper < 0) upper = 0.0;

        return getBarSeries().numFactory().numOf(upper);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
