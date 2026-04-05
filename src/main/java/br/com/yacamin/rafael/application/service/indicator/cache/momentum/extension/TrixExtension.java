package br.com.yacamin.rafael.application.service.indicator.cache.momentum.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

/**
 * TRIX = ((ema3[t] - ema3[t-1]) / ema3[t-1]) * 100
 * where ema3 = EMA(EMA(EMA(close, period), period), period)
 */
public class TrixExtension extends CachedIndicator<Num> {

    private final EMAIndicator ema3;

    public TrixExtension(ClosePriceIndicator close, int period) {
        super(close.getBarSeries());
        var ema1 = new EMAIndicator(close, period);
        var ema2 = new EMAIndicator(ema1, period);
        this.ema3 = new EMAIndicator(ema2, period);
    }

    @Override
    protected Num calculate(int index) {
        if (index <= 0) {
            return getBarSeries().numFactory().numOf(0);
        }

        double current = ema3.getValue(index).doubleValue();
        double previous = ema3.getValue(index - 1).doubleValue();

        if (Math.abs(previous) < 1e-12) {
            return getBarSeries().numFactory().numOf(0);
        }

        double trix = ((current - previous) / previous) * 100.0;
        return getBarSeries().numFactory().numOf(trix);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
