package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Roll Cov (raw): mean( ΔP_t * ΔP_{t-1} ) over window
 * where ΔP_t = close_t - close_{t-1}
 */
public class RollCovIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> close;
    private final int window;

    public RollCovIndicator(Indicator<Num> close, int window) {
        super(close.getBarSeries());
        this.close = close;
        this.window = window;
    }

    private double delta(int idx) {
        if (idx <= 0) return 0.0;
        double c1 = close.getValue(idx).doubleValue();
        double c0 = close.getValue(idx - 1).doubleValue();
        return c1 - c0;
    }

    @Override
    protected Num calculate(int index) {
        if (index <= 0) {
            return getBarSeries().numFactory().numOf(0);
        }

        int windowStart = Math.max(1, index - window + 1);

        double sumProducts = 0.0;
        int count = 0;

        for (int i = windowStart; i <= index; i++) {
            double deltaNow  = delta(i);
            double deltaPrev = delta(i - 1);
            sumProducts += (deltaNow * deltaPrev);
            count++;
        }

        double rollCov = (count == 0) ? 0.0 : (sumProducts / count);
        return getBarSeries().numFactory().numOf(rollCov);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
