package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class OfiZscoreIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> ofi;
    private final int window;

    public OfiZscoreIndicator(Indicator<Num> ofi, int window) {
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

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = ofi.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double var = (sumSq / n) - (mean * mean);
        if (var < 0 && var > -1e-9) var = 0.0;

        double sd = Math.sqrt(var);
        if (!Double.isFinite(sd) || sd < EPS) return getBarSeries().numFactory().numOf(0);

        double last = ofi.getValue(index).doubleValue();
        return getBarSeries().numFactory().numOf((last - mean) / sd);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
