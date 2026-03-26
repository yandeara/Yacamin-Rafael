package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class LogRangeIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> range;

    public LogRangeIndicator(Indicator<Num> range) {
        super(range.getBarSeries());
        this.range = range;
    }

    @Override
    protected Num calculate(int index) {
        double r = range.getValue(index).doubleValue();
        double v = (r < EPS) ? 0.0 : Math.log(r);
        if (!Double.isFinite(v)) v = 0.0;
        return getBarSeries().numFactory().numOf(v);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
