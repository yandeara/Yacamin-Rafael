package br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Z-Score do Amihud ILLIQ sobre uma janela.
 * zscore = (current - mean) / std
 * Se std ≈ 0, retorna 0.
 */
public class AmihudZscoreExtension extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> source;
    private final int window;

    public AmihudZscoreExtension(Indicator<Num> source, int window) {
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
        double mean = sum / window;

        double sumSq = 0;
        for (int i = 0; i < window; i++) {
            double diff = source.getValue(index - i).doubleValue() - mean;
            sumSq += diff * diff;
        }
        double std = Math.sqrt(sumSq / window);

        if (std < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        double current = source.getValue(index).doubleValue();
        return getBarSeries().numFactory().numOf((current - mean) / std);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
