package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class BodyAbsPctIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> bodyAbs;

    public BodyAbsPctIndicator(Indicator<Num> bodyAbs) {
        super(bodyAbs.getBarSeries());
        this.bodyAbs = bodyAbs;
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);

        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        if (!Double.isFinite(range) || range < EPS) return getBarSeries().numFactory().numOf(0);

        double v = bodyAbs.getValue(index).doubleValue() / (range + EPS);
        if (!Double.isFinite(v)) v = 0.0;

        return getBarSeries().numFactory().numOf(v);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
