package br.com.yacamin.rafael.application.service.indicator.cache.range.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Lagged Mean: SMA que EXCLUI a barra atual (lag=1).
 * mean(source[i-window..i-1]) = sum(source[i-1], source[i-2], ..., source[i-window]) / window
 */
public class RangeLaggedMeanExtension extends CachedIndicator<Num> {

    private final Indicator<Num> source;
    private final int window;

    public RangeLaggedMeanExtension(Indicator<Num> source, int window) {
        super(source.getBarSeries());
        this.source = source;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (index < window) {
            return getBarSeries().numFactory().numOf(0);
        }

        double sum = 0;
        for (int i = 1; i <= window; i++) {
            sum += source.getValue(index - i).doubleValue();
        }

        return getBarSeries().numFactory().numOf(sum / window);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window + 1;
    }
}
