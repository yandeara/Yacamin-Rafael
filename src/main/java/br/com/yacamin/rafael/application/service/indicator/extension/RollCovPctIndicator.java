package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Roll Cov Pct: mean( ΔP%_t * ΔP%_{t-1} ) over window
 * where ΔP%_t = (close_t - close_{t-1}) / close_{t-1}
 */
public class RollCovPctIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> close;
    private final int window;

    public RollCovPctIndicator(Indicator<Num> close, int window) {
        super(close.getBarSeries());
        this.close = close;
        this.window = window;
    }

    private double deltaPct(int idx) {
        if (idx <= 0) return 0.0;

        double c1 = close.getValue(idx).doubleValue();
        double c0 = close.getValue(idx - 1).doubleValue();

        if (Math.abs(c0) < EPS) return 0.0;
        return (c1 - c0) / c0;
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
            double deltaNow  = deltaPct(i);
            double deltaPrev = deltaPct(i - 1);
            sumProducts += (deltaNow * deltaPrev);
            count++;
        }

        double rollCovPct = (count == 0) ? 0.0 : (sumProducts / count);
        return getBarSeries().numFactory().numOf(rollCovPct);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
