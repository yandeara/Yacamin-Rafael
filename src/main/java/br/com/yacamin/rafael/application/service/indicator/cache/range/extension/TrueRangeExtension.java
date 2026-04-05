package br.com.yacamin.rafael.application.service.indicator.cache.range.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * True Range = max(high - low, |high - prevClose|, |low - prevClose|)
 */
public class TrueRangeExtension extends CachedIndicator<Num> {

    public TrueRangeExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (index <= 0) {
            return getBarSeries().numFactory().numOf(range);
        }

        double prevClose = getBarSeries().getBar(index - 1).getClosePrice().doubleValue();
        double tr = Math.max(range, Math.max(Math.abs(high - prevClose), Math.abs(low - prevClose)));
        return getBarSeries().numFactory().numOf(tr);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 1;
    }
}
