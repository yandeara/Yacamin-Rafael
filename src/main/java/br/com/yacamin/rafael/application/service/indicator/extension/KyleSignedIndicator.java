package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Kyle Signed (instant impact):
 * signed = ret_t / ofi_t
 */
public class KyleSignedIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> close;
    private final Indicator<Num> ofi;

    public KyleSignedIndicator(Indicator<Num> close, Indicator<Num> ofi) {
        super(close.getBarSeries());
        this.close = close;
        this.ofi = ofi;
    }

    @Override
    protected Num calculate(int index) {
        if (index <= 0) return getBarSeries().numFactory().numOf(0);

        double c1 = close.getValue(index).doubleValue();
        double c0 = close.getValue(index - 1).doubleValue();
        if (Math.abs(c0) < EPS) return getBarSeries().numFactory().numOf(0);

        double ret = (c1 - c0) / c0;

        double x = ofi.getValue(index).doubleValue();
        if (Math.abs(x) < EPS) return getBarSeries().numFactory().numOf(0);

        return getBarSeries().numFactory().numOf(ret / x);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 1;
    }
}
