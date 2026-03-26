package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class MeanIgnoreZeroIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> base;
    private final int window;

    public MeanIgnoreZeroIndicator(Indicator<Num> base, int window) {
        super(base.getBarSeries());
        this.base = base;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 0 || index < 0) return getBarSeries().numFactory().numOf(0);

        int start = Math.max(0, index - window + 1);

        double sum = 0.0;
        int n = 0;

        for (int i = start; i <= index; i++) {
            double v = base.getValue(i).doubleValue();
            if (Math.abs(v) < EPS) continue; // ignora zeros (range<eps => logRange=0)
            sum += v;
            n++;
        }

        return getBarSeries().numFactory().numOf(n == 0 ? 0.0 : (sum / n));
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
