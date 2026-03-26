package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class BodyPercIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> body;

    public BodyPercIndicator(Indicator<Num> body) {
        super(body.getBarSeries());
        this.body = body;
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);

        double open = bar.getOpenPrice().doubleValue();
        if (!Double.isFinite(open) || Math.abs(open) < EPS) return getBarSeries().numFactory().numOf(0);

        double v = body.getValue(index).doubleValue() / (open + EPS);
        if (!Double.isFinite(v)) v = 0.0;

        return getBarSeries().numFactory().numOf(v);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
