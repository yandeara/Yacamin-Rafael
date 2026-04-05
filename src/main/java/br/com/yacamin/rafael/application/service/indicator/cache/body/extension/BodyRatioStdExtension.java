package br.com.yacamin.rafael.application.service.indicator.cache.body.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Desvio padrao do body ratio sobre uma janela.
 * STD(bodyRatio, window) = sqrt(sum((x - mean)^2) / window)
 */
public class BodyRatioStdExtension extends CachedIndicator<Num> {

    private final Indicator<Num> source;
    private final int window;

    public BodyRatioStdExtension(Indicator<Num> source, int window) {
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

        return getBarSeries().numFactory().numOf(Math.sqrt(sumSq / window));
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
