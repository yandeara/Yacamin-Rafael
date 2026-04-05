package br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * AMIHUD ILLIQ (raw) = |(C_t - C_{t-1}) / C_{t-1}| / Volume_t
 */
public class AmihudExtension extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> close;

    public AmihudExtension(Indicator<Num> close) {
        super(close.getBarSeries());
        this.close = close;
    }

    @Override
    protected Num calculate(int index) {
        if (index <= 0) {
            return getBarSeries().numFactory().numOf(0);
        }

        double c1 = close.getValue(index).doubleValue();
        double c0 = close.getValue(index - 1).doubleValue();

        if (Math.abs(c0) < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        double volume = getBarSeries().getBar(index).getVolume().doubleValue();
        if (Math.abs(volume) < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        double ret = (c1 - c0) / c0;
        double absRet = Math.abs(ret);

        return getBarSeries().numFactory().numOf(absRet / volume);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 1;
    }
}
