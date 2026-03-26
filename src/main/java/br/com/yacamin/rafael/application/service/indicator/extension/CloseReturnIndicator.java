package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class CloseReturnIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> close;
    private final int window;

    public CloseReturnIndicator(Indicator<Num> close, int window) {
        super(close.getBarSeries());
        this.close = close;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        int prevIndex = index - window;
        if (prevIndex < 0) {
            return getBarSeries().numFactory().numOf(0);
        }

        double closeNow  = close.getValue(index).doubleValue();
        double closePrev = close.getValue(prevIndex).doubleValue();

        if (Math.abs(closePrev) < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        double ret = (closeNow / closePrev) - 1.0;
        return getBarSeries().numFactory().numOf(ret);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
