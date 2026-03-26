package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class BurstStrengthIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> ret1;
    private final int window;

    public BurstStrengthIndicator(Indicator<Num> ret1, int window) {
        super(ret1.getBarSeries());
        this.ret1 = ret1;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 0 || index < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, index - window + 1);

        double maxAbs = 0.0;
        for (int i = start; i <= index; i++) {
            double v = Math.abs(ret1.getValue(i).doubleValue());
            if (v > maxAbs) maxAbs = v;
        }

        return getBarSeries().numFactory().numOf(maxAbs);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
