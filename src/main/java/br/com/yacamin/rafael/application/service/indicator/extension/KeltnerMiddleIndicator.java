package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class KeltnerMiddleIndicator extends CachedIndicator<Num> {

    private final EMAIndicator ema;
    private final ClosePriceIndicator closePrice;

    public KeltnerMiddleIndicator(BarSeries series, int emaPeriod) {
        super(series);
        this.closePrice = new ClosePriceIndicator(series);
        this.ema = new EMAIndicator(closePrice, emaPeriod);
    }

    @Override
    protected Num calculate(int index) {
        return ema.getValue(index);
    }

    @Override
    public int getCountOfUnstableBars() {
        return ema.getCountOfUnstableBars();
    }
}
