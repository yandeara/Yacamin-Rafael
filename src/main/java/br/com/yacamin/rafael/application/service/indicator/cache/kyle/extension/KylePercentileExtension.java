package br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Percentile do Kyle Lambda sobre uma janela.
 * percentile = count(source[i] <= current) / window
 */
public class KylePercentileExtension extends CachedIndicator<Num> {

    private final Indicator<Num> source;
    private final int window;

    public KylePercentileExtension(Indicator<Num> source, int window) {
        super(source.getBarSeries());
        this.source = source;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (index < window - 1) {
            return getBarSeries().numFactory().numOf(0);
        }

        double current = source.getValue(index).doubleValue();
        int count = 0;
        for (int i = 0; i < window; i++) {
            if (source.getValue(index - i).doubleValue() <= current) {
                count++;
            }
        }

        return getBarSeries().numFactory().numOf((double) count / window);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
