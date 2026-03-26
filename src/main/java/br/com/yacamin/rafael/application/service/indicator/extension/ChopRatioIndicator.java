package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class ChopRatioIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> ret1;
    private final int window;

    public ChopRatioIndicator(Indicator<Num> ret1, int window) {
        super(ret1.getBarSeries());
        this.ret1 = ret1;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 1 || index < 0) return getBarSeries().numFactory().numOf(0);

        int start = index - window + 1;
        if (start < 0) return getBarSeries().numFactory().numOf(0);

        int changes = 0;

        double prevSign = Math.signum(ret1.getValue(index).doubleValue());

        for (int i = 1; i < window; i++) {
            double s = Math.signum(ret1.getValue(index - i).doubleValue());

            if (s != 0 && prevSign != 0 && s != prevSign) {
                changes++;
            }
            if (s != 0) prevSign = s;
        }

        double ratio = changes / (double) (window - 1);
        return getBarSeries().numFactory().numOf(ratio);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
