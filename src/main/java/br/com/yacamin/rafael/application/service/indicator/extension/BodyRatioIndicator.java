package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class BodyRatioIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> body;

    public BodyRatioIndicator(Indicator<Num> body) {
        super(body.getBarSeries());
        this.body = body;
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);

        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        if (!Double.isFinite(range) || range < EPS) return getBarSeries().numFactory().numOf(0);

        double b = body.getValue(index).doubleValue();
        double ratio = b / (range + EPS);
        if (!Double.isFinite(ratio)) ratio = 0.0;

        return getBarSeries().numFactory().numOf(ratio);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
