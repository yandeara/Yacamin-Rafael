package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class OfiPersistenceIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> ofi;
    private final int window;

    public OfiPersistenceIndicator(Indicator<Num> ofi, int window) {
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
        if (window <= 0) return getBarSeries().numFactory().numOf(0);

        int start = index - window + 1;
        if (start < 0) return getBarSeries().numFactory().numOf(0);

        double now = sign(ofi.getValue(index).doubleValue());
        if (now == 0.0) return getBarSeries().numFactory().numOf(0);

        int same = 0;
        for (int i = start; i <= index; i++) {
            if (sign(ofi.getValue(i).doubleValue()) == now) same++;
        }

        return getBarSeries().numFactory().numOf((double) same / (double) window);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
