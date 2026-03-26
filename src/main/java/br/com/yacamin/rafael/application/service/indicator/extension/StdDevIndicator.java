package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class StdDevIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> base;
    private final int window;

    public StdDevIndicator(Indicator<Num> base, int window) {
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

        double sum = 0.0, sumSq = 0.0;
        for (int i = start; i <= index; i++) {
            double v = base.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double var = (sumSq / n) - (mean * mean);
        if (var < 0 && var > -1e-9) var = 0.0;

        double sd = Math.sqrt(Math.max(0.0, var));
        if (!Double.isFinite(sd) || sd < EPS) sd = 0.0;

        return getBarSeries().numFactory().numOf(sd);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
