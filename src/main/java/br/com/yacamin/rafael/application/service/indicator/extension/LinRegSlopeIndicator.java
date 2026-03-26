package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class LinRegSlopeIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> base;
    private final int window;

    public LinRegSlopeIndicator(Indicator<Num> base, int window) {
        super(base.getBarSeries());
        this.base = base;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 1 || index < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;
        if (n < 2) return getBarSeries().numFactory().numOf(0);

        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
        int k = 0;

        for (int i = start; i <= index; i++) {
            double x = k++;
            double y = base.getValue(i).doubleValue();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);
        if (Math.abs(denom) < EPS) return getBarSeries().numFactory().numOf(0);

        double slope = (n * sumXY - sumX * sumY) / denom;
        if (!Double.isFinite(slope)) slope = 0.0;

        return getBarSeries().numFactory().numOf(slope);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
