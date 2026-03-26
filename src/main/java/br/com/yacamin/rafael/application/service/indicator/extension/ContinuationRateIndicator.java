package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class ContinuationRateIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> ret1;
    private final int window;

    public ContinuationRateIndicator(Indicator<Num> ret1, int window) {
        super(ret1.getBarSeries());
        this.ret1 = ret1;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 0 || index < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, index - window + 1);

        double sum = 0.0;
        for (int i = start; i <= index; i++) sum += ret1.getValue(i).doubleValue();
        if (sum == 0.0) return getBarSeries().numFactory().numOf(0);

        double dominantSign = sum > 0 ? 1.0 : -1.0;

        int aligned = 0;
        int n = 0;

        for (int i = start; i <= index; i++) {
            double r = ret1.getValue(i).doubleValue();
            if (r != 0.0 && Math.signum(r) == dominantSign) aligned++;
            n++;
        }

        return getBarSeries().numFactory().numOf(n == 0 ? 0.0 : (double) aligned / (double) n);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
