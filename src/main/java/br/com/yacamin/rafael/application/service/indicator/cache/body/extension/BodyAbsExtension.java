package br.com.yacamin.rafael.application.service.indicator.cache.body.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Valor absoluto do body: |close - open|.
 */
public class BodyAbsExtension extends CachedIndicator<Num> {

    private final Indicator<Num> source;

    public BodyAbsExtension(Indicator<Num> source) {
        super(source.getBarSeries());
        this.source = source;
    }

    @Override
    protected Num calculate(int index) {
        double value = Math.abs(source.getValue(index).doubleValue());
        return getBarSeries().numFactory().numOf(value);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
