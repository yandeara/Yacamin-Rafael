package br.com.yacamin.rafael.application.service.indicator.cache.shape.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * SMA generico para indicadores de shape sobre uma janela.
 * SMA(source, window) = sum(source[i-window+1..i]) / window
 */
public class ShapeSmaExtension extends CachedIndicator<Num> {

    private final Indicator<Num> source;
    private final int window;

    public ShapeSmaExtension(Indicator<Num> source, int window) {
        super(source.getBarSeries());
        this.source = source;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (index < window - 1) {
            return getBarSeries().numFactory().numOf(0);
        }

        double sum = 0;
        for (int i = 0; i < window; i++) {
            sum += source.getValue(index - i).doubleValue();
        }

        return getBarSeries().numFactory().numOf(sum / window);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
