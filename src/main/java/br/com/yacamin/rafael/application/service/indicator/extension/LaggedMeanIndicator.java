package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class LaggedMeanIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> base;
    private final int window;
    private final int lag;

    public LaggedMeanIndicator(Indicator<Num> base, int window, int lag) {
        super(base.getBarSeries());
        this.base = base;
        this.window = window;
        this.lag = Math.max(0, lag);
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 0 || index < 0) return getBarSeries().numFactory().numOf(0);

        int end = index - lag;
        if (end < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, end - window + 1);

        double sum = 0.0;
        int n = 0;

        for (int i = start; i <= end; i++) {
            sum += base.getValue(i).doubleValue();
            n++;
        }

        return getBarSeries().numFactory().numOf(n == 0 ? 0.0 : (sum / n));
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
