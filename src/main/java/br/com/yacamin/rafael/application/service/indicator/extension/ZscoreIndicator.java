package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Zscore do valor atual vs janela (inclui o próprio valor atual na janela):
 * z = (x_last - mean) / std
 */
public class ZscoreIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> base;
    private final int window;

    public ZscoreIndicator(Indicator<Num> base, int window) {
        super(base.getBarSeries());
        this.base = base;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (window <= 1 || index < 0) {
            return getBarSeries().numFactory().numOf(0);
        }

        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;
        if (n < 2) return getBarSeries().numFactory().numOf(0);

        double sum = 0.0;
        for (int i = start; i <= index; i++) sum += base.getValue(i).doubleValue();
        double mean = sum / n;

        double var = 0.0;
        for (int i = start; i <= index; i++) {
            double d = base.getValue(i).doubleValue() - mean;
            var += d * d;
        }
        double std = Math.sqrt(var / n);
        if (!Double.isFinite(std) || std < EPS) return getBarSeries().numFactory().numOf(0);

        double x = base.getValue(index).doubleValue();
        double z = (x - mean) / std;

        return getBarSeries().numFactory().numOf(z);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
