package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class BodyRunLenIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> body;
    private final int maxLookback;

    public BodyRunLenIndicator(Indicator<Num> body, int maxLookback) {
        super(body.getBarSeries());
        this.body = body;
        this.maxLookback = Math.max(1, maxLookback);
    }

    @Override
    protected Num calculate(int index) {
        if (index < 0) return getBarSeries().numFactory().numOf(0);

        double now = body.getValue(index).doubleValue();
        if (!Double.isFinite(now) || Math.abs(now) < EPS) return getBarSeries().numFactory().numOf(0);

        int sign = now > 0 ? 1 : -1;
        int count = 0;

        int start = Math.max(0, index - maxLookback + 1);
        for (int i = index; i >= start; i--) {
            double v = body.getValue(i).doubleValue();
            if (!Double.isFinite(v) || Math.abs(v) < EPS) break;

            int s = v > 0 ? 1 : -1;
            if (s != sign) break;

            count++;
        }

        return getBarSeries().numFactory().numOf(sign * (double) count);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
