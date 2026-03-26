package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class ClosePosNormIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    public ClosePosNormIndicator(org.ta4j.core.BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);

        double h = bar.getHighPrice().doubleValue();
        double l = bar.getLowPrice().doubleValue();
        double c = bar.getClosePrice().doubleValue();

        double range = h - l;
        if (!Double.isFinite(range) || range < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        double v = (c - l) / (range + EPS);
        if (!Double.isFinite(v)) v = 0.0;

        return getBarSeries().numFactory().numOf(v);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
