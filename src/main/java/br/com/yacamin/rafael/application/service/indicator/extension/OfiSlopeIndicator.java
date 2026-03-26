package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class OfiSlopeIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> ofi;
    private final int window;

    public OfiSlopeIndicator(Indicator<Num> ofi, int window) {
        super(ofi.getBarSeries());
        this.ofi = ofi;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 1) return getBarSeries().numFactory().numOf(0);
        if (index < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;
        if (n < 2) return getBarSeries().numFactory().numOf(0);

        double sumX = 0.0, sumY = 0.0, sumXY = 0.0, sumX2 = 0.0;
        int k = 0;

        for (int i = start; i <= index; i++) {
            double x = k++;
            double y = ofi.getValue(i).doubleValue();

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
