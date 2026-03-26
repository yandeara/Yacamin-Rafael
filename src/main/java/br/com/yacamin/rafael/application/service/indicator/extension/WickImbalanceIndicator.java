package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class WickImbalanceIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> upperWick;
    private final Indicator<Num> lowerWick;

    public WickImbalanceIndicator(Indicator<Num> upperWick, Indicator<Num> lowerWick) {
        super(upperWick.getBarSeries());
        this.upperWick = upperWick;
        this.lowerWick = lowerWick;
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);

        double range = bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue();
        if (!Double.isFinite(range) || range < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        double upper = upperWick.getValue(index).doubleValue();
        double lower = lowerWick.getValue(index).doubleValue();

        double imb = (upper - lower) / (range + EPS);
        if (!Double.isFinite(imb)) imb = 0.0;

        return getBarSeries().numFactory().numOf(imb);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
