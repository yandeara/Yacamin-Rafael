package br.com.yacamin.rafael.application.service.indicator.cache.body.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Fracao de barras positivas na janela.
 * signPersistence = count(body > EPS) / window
 */
public class BodySignPersistenceExtension extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> bodySource;
    private final int window;

    public BodySignPersistenceExtension(Indicator<Num> bodySource, int window) {
        super(bodySource.getBarSeries());
        this.bodySource = bodySource;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (index < window - 1) {
            return getBarSeries().numFactory().numOf(0);
        }

        int positives = 0;
        for (int i = 0; i < window; i++) {
            if (bodySource.getValue(index - i).doubleValue() > EPS) {
                positives++;
            }
        }

        return getBarSeries().numFactory().numOf((double) positives / window);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
