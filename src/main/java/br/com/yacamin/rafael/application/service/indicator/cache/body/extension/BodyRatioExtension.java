package br.com.yacamin.rafael.application.service.indicator.cache.body.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Body ratio = (close - open) / (high - low).
 * Retorna 0 se range < EPS.
 */
public class BodyRatioExtension extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    public BodyRatioExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        double close = bar.getClosePrice().doubleValue();
        double open = bar.getOpenPrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double range = high - low;

        if (Math.abs(range) < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        return getBarSeries().numFactory().numOf((close - open) / range);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
