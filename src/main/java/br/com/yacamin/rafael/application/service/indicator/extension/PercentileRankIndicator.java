package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class PercentileRankIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> base;
    private final int window;

    public PercentileRankIndicator(Indicator<Num> base, int window) {
        super(base.getBarSeries());
        this.base = base;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 1 || index < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;
        if (n <= 0) return getBarSeries().numFactory().numOf(0);

        double cur = base.getValue(index).doubleValue();

        int less = 0;
        int equal = 0;

        for (int i = start; i <= index; i++) {
            double v = base.getValue(i).doubleValue();
            if (v < cur) less++;
            else if (v == cur) equal++;
        }

        // “midrank”: less + 0.5*equal (normaliza em [0,1])
        double rank = (less + 0.5 * equal) / (double) n;
        if (!Double.isFinite(rank)) rank = 0.0;

        return getBarSeries().numFactory().numOf(rank);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
