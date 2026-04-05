package br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

/**
 * Keltner Middle = EMA(close, period)
 */
public class KeltnerMiddleExtension extends CachedIndicator<Num> {

    private final EMAIndicator ema;

    public KeltnerMiddleExtension(ClosePriceIndicator close, int period) {
        super(close.getBarSeries());
        this.ema = new EMAIndicator(close, period);
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
