package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class Hlc3Indicator extends CachedIndicator<Num> {

    public Hlc3Indicator(org.ta4j.core.BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var b = getBarSeries().getBar(index);

        double hlc3 = (b.getHighPrice().doubleValue()
                + b.getLowPrice().doubleValue()
                + b.getClosePrice().doubleValue()) / 3.0;

        if (!Double.isFinite(hlc3)) hlc3 = 0.0;
        return getBarSeries().numFactory().numOf(hlc3);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
