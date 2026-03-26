package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class BodyAbsIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> body;

    public BodyAbsIndicator(Indicator<Num> body) {
        super(body.getBarSeries());
        this.body = body;
    }

    @Override
    protected Num calculate(int index) {
        double v = Math.abs(body.getValue(index).doubleValue());
        if (!Double.isFinite(v)) v = 0.0;
        return getBarSeries().numFactory().numOf(v);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
