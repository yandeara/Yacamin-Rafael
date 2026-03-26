package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class DecayRateIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> ret1;
    private final int window;

    public DecayRateIndicator(Indicator<Num> ret1, int window) {
        super(ret1.getBarSeries());
        this.ret1 = ret1;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 1 || index < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;
        if (n <= 1) return getBarSeries().numFactory().numOf(0);

        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
        int k = 0;

        for (int i = start; i <= index; i++) {
            double x = k++;
            double y = Math.abs(ret1.getValue(i).doubleValue());

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denom = n * sumX2 - (sumX * sumX);
        if (Math.abs(denom) < EPS) return getBarSeries().numFactory().numOf(0);

        double slope = (n * sumXY - sumX * sumY) / denom;
        return getBarSeries().numFactory().numOf(slope);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
