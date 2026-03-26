package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Hasbrouck Lambda:
 * lambda = Σ(|ret_t| * svr_t) / Σ(svr_t^2) over window
 *
 * |ret_t| = abs((close_t - close_{t-1}) / close_{t-1})
 */
public class HasbrouckLambdaIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> close;
    private final Indicator<Num> svr;
    private final int window;

    public HasbrouckLambdaIndicator(Indicator<Num> close, Indicator<Num> svr, int window) {
        super(close.getBarSeries());
        this.close = close;
        this.svr = svr;
        this.window = window;
    }

    private double absReturn(int i) {
        if (i <= 0) return 0.0;

        double c1 = close.getValue(i).doubleValue();
        double c0 = close.getValue(i - 1).doubleValue();

        if (!Double.isFinite(c0) || Math.abs(c0) < EPS) return 0.0;
        double r = (c1 - c0) / c0;
        if (!Double.isFinite(r)) return 0.0;

        return Math.abs(r);
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
            double absRet = absReturn(i);
            double x = svr.getValue(i).doubleValue();

            sumXY += absRet * x;
            sumXX += x * x;
        }

        double lambda = (Math.abs(sumXX) < EPS) ? 0.0 : (sumXY / sumXX);
        if (!Double.isFinite(lambda)) lambda = 0.0;

        return getBarSeries().numFactory().numOf(lambda);
    }

    @Override
    public int getCountOfUnstableBars() {
        return Math.max(1, window);
    }
}
