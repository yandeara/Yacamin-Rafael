package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class BodyIndicator extends CachedIndicator<Num> {

    public BodyIndicator(org.ta4j.core.BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);

        double body = bar.getClosePrice().doubleValue() - bar.getOpenPrice().doubleValue();
        if (!Double.isFinite(body)) body = 0.0;

        return getBarSeries().numFactory().numOf(body);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
