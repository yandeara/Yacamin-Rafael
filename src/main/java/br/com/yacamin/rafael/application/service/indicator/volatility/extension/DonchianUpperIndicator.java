package br.com.yacamin.rafael.application.service.indicator.volatility.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.num.Num;

public class DonchianUpperIndicator extends CachedIndicator<Num> {

    private final HighPriceIndicator high;
    private final int period;

    public DonchianUpperIndicator(BarSeries series, int period) {
        super(series);
        this.high = new HighPriceIndicator(series);
        this.period = period;
    }

    @Override
    protected Num calculate(int index) {
        int start = Math.max(0, index - period + 1);
        Num highest = high.getValue(start);

        for (int i = start + 1; i <= index; i++) {
            if (high.getValue(i).isGreaterThan(highest)) {
                highest = high.getValue(i);
            }
        }
        return highest;
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
