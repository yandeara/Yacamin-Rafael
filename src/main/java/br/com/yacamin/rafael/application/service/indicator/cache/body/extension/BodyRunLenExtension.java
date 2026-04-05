package br.com.yacamin.rafael.application.service.indicator.cache.body.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Comprimento da sequencia consecutiva de barras com mesmo sinal do body.
 * Positivo se bullish run, negativo se bearish run.
 */
public class BodyRunLenExtension extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> bodySource;
    private final int maxLookback;

    public BodyRunLenExtension(Indicator<Num> bodySource, int maxLookback) {
        super(bodySource.getBarSeries());
        this.bodySource = bodySource;
        this.maxLookback = maxLookback;
    }

    @Override
    protected Num calculate(int index) {
        double current = bodySource.getValue(index).doubleValue();

        if (Math.abs(current) < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        boolean positive = current > 0;
        int count = 1;

        for (int i = 1; i <= Math.min(maxLookback, index); i++) {
            double prev = bodySource.getValue(index - i).doubleValue();
            if ((positive && prev > EPS) || (!positive && prev < -EPS)) {
                count++;
            } else {
                break;
            }
        }

        return getBarSeries().numFactory().numOf(positive ? count : -count);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 1;
    }
}
