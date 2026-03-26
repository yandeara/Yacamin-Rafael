package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Kyle Lambda (Roll-style regression):
 * lambda = sum(ret_t * ofi_t) / sum(ofi_t^2) over window
 *
 * ret_t = (close_t - close_{t-1}) / close_{t-1}
 */
public class KyleLambdaIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> close;
    private final Indicator<Num> ofi;
    private final int window;

    public KyleLambdaIndicator(Indicator<Num> close, Indicator<Num> ofi, int window) {
        super(close.getBarSeries());
        this.close = close;
        this.ofi = ofi;
        this.window = window;
    }

    private double ret(int i) {
        if (i <= 0) return 0.0;

        double c1 = close.getValue(i).doubleValue();
        double c0 = close.getValue(i - 1).doubleValue();

        if (Math.abs(c0) < EPS) return 0.0;
        return (c1 - c0) / c0;
    }

    @Override
    protected Num calculate(int index) {
        if (index <= 0 || window <= 1) {
            return getBarSeries().numFactory().numOf(0);
        }

        int start = Math.max(1, index - window + 1);

        double sumXY = 0.0;
        double sumXX = 0.0;

        for (int i = start; i <= index; i++) {
            double r = ret(i);
            double x = ofi.getValue(i).doubleValue();

            sumXY += (r * x);
            sumXX += (x * x);
        }

        double lambda = (Math.abs(sumXX) < EPS) ? 0.0 : (sumXY / sumXX);
        return getBarSeries().numFactory().numOf(lambda);
    }

    @Override
    public int getCountOfUnstableBars() {
        // precisa de close_{t-1}
        return 1;
    }
}
