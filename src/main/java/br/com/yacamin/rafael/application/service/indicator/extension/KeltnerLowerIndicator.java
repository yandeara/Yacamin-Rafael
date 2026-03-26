package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.num.Num;

public class KeltnerLowerIndicator extends CachedIndicator<Num> {

    private final KeltnerMiddleIndicator middle;
    private final ATRIndicator atr;
    private final Num multiplier;

    public KeltnerLowerIndicator(BarSeries series,
                                 int emaPeriod,
                                 int atrPeriod,
                                 double multiplier) {
        super(series);

        this.middle = new KeltnerMiddleIndicator(series, emaPeriod);
        this.atr = new ATRIndicator(series, atrPeriod);
        this.multiplier = getBarSeries().numFactory().numOf(multiplier);
    }

    @Override
    protected Num calculate(int index) {
        return middle.getValue(index)
                .minus(atr.getValue(index).multipliedBy(multiplier));
    }

    @Override
    public int getCountOfUnstableBars() {
        return Math.max(
                middle.getCountOfUnstableBars(),
                atr.getCountOfUnstableBars()
        );
    }
}
