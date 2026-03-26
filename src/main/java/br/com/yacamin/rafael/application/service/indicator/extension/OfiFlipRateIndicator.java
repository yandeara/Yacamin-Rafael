package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class OfiFlipRateIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> ofi;
    private final int window;

    public OfiFlipRateIndicator(Indicator<Num> ofi, int window) {
        super(ofi.getBarSeries());
        this.ofi = ofi;
        this.window = window;
    }

    private double sign(double x) {
        if (x > EPS) return 1.0;
        if (x < -EPS) return -1.0;
        return 0.0;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 1) return getBarSeries().numFactory().numOf(0);

        int start = index - window + 1;
        if (start < 1) return getBarSeries().numFactory().numOf(0);

        int flips = 0;
        double prev = sign(ofi.getValue(start - 1).doubleValue());

        for (int i = start; i <= index; i++) {
            double cur = sign(ofi.getValue(i).doubleValue());
            if (cur != 0.0 && prev != 0.0 && cur != prev) flips++;
            if (cur != 0.0) prev = cur;
        }

        return getBarSeries().numFactory().numOf((double) flips / (double) window);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
