package br.com.yacamin.rafael.application.service.indicator.volatility.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.Num;

public class DonchianLowerIndicator extends CachedIndicator<Num> {

    private final LowPriceIndicator low;
    private final int period;

    public DonchianLowerIndicator(BarSeries series, int period) {
        super(series);
        this.low = new LowPriceIndicator(series);
        this.period = period;
    }

    @Override
    protected Num calculate(int index) {
        int start = Math.max(0, index - period + 1);
        Num lowest = low.getValue(start);

        for (int i = start + 1; i <= index; i++) {
            if (low.getValue(i).isLessThan(lowest)) {
                lowest = low.getValue(i);
            }
        }
        return lowest;
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
