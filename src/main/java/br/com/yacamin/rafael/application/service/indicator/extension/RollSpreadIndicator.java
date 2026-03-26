package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Roll Spread = 2 * sqrt(-cov) if cov < 0 else 0
 */
public class RollSpreadIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> cov;

    public RollSpreadIndicator(Indicator<Num> cov) {
        super(cov.getBarSeries());
        this.cov = cov;
    }

    @Override
    protected Num calculate(int index) {
        double c = cov.getValue(index).doubleValue();

        double spread;
        if (!Double.isFinite(c) || c >= 0.0) spread = 0.0;
        else spread = 2.0 * Math.sqrt(-c);

        return getBarSeries().numFactory().numOf(spread);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
