package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class OfiRelIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> ofi;
    private final int window;

    public OfiRelIndicator(Indicator<Num> ofi, int window) {
        super(ofi.getBarSeries());
        this.ofi = ofi;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 0) return getBarSeries().numFactory().numOf(0);
        if (index < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, index - window + 1);

        double sumSq = 0.0;
        int n = 0;
        for (int i = start; i <= index; i++) {
            double x = ofi.getValue(i).doubleValue();
            sumSq += x * x;
            n++;
        }

        if (n == 0) return getBarSeries().numFactory().numOf(0);

        double rms = Math.sqrt(sumSq / n);
        if (!Double.isFinite(rms) || rms < EPS) return getBarSeries().numFactory().numOf(0);

        double now = ofi.getValue(index).doubleValue();
        return getBarSeries().numFactory().numOf(now / rms);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
