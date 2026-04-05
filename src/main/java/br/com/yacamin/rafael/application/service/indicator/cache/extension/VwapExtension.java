package br.com.yacamin.rafael.application.service.indicator.cache.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * VWAP cumulativo (sem janela) = sum(typicalPrice * volume) / sum(volume)
 * desde o inicio da serie.
 * typicalPrice = (high + low + close) / 3
 */
public class VwapExtension extends CachedIndicator<Num> {

    private Num cumulativePV;
    private Num cumulativeV;
    private int lastIndex;

    public VwapExtension(BarSeries series) {
        super(series);
        var factory = series.numFactory();
        this.cumulativePV = factory.zero();
        this.cumulativeV = factory.zero();
        this.lastIndex = -1;
    }

    @Override
    protected Num calculate(int index) {
        BarSeries series = getBarSeries();
        var factory = series.numFactory();

        int start = Math.max(lastIndex + 1, 0);

        for (int i = start; i <= index; i++) {
            Bar bar = series.getBar(i);
            Num price = bar.getHighPrice()
                    .plus(bar.getLowPrice())
                    .plus(bar.getClosePrice())
                    .dividedBy(factory.numOf(3));
            Num volume = bar.getVolume();
            cumulativePV = cumulativePV.plus(price.multipliedBy(volume));
            cumulativeV = cumulativeV.plus(volume);
        }

        lastIndex = index;

        if (cumulativeV.isZero()) {
            return factory.zero();
        }

        return cumulativePV.dividedBy(cumulativeV);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 1;
    }
}
