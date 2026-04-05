package br.com.yacamin.rafael.application.service.indicator.cache.range.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * LogRange = ln(range), onde range vem do RangeExtension.
 */
public class LogRangeExtension extends CachedIndicator<Num> {

    private final Indicator<Num> source;

    public LogRangeExtension(Indicator<Num> source) {
        super(source.getBarSeries());
        this.source = source;
    }

    @Override
    protected Num calculate(int index) {
        double range = source.getValue(index).doubleValue();
        if (range < 1e-12) {
            return getBarSeries().numFactory().numOf(0);
        }
        return getBarSeries().numFactory().numOf(Math.log(range));
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
