package br.com.yacamin.rafael.application.service.indicator.volatility.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class DonchianMiddleIndicator extends CachedIndicator<Num> {

    private final DonchianUpperIndicator upper;
    private final DonchianLowerIndicator lower;

    public DonchianMiddleIndicator(BarSeries series, int period) {
        super(series);
        this.upper = new DonchianUpperIndicator(series, period);
        this.lower = new DonchianLowerIndicator(series, period);
    }

    @Override
    protected Num calculate(int index) {
        return upper.getValue(index).plus(lower.getValue(index)).dividedBy(getBarSeries().numFactory().numOf(2));
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
