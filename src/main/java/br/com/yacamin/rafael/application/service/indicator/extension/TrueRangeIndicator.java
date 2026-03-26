package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class TrueRangeIndicator extends CachedIndicator<Num> {

    public TrueRangeIndicator(org.ta4j.core.BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = getBarSeries().getBar(index);

        double h = bar.getHighPrice().doubleValue();
        double l = bar.getLowPrice().doubleValue();

        if (index <= 0) {
            double tr0 = h - l;
            if (!Double.isFinite(tr0) || tr0 < 0) tr0 = 0.0;
            return getBarSeries().numFactory().numOf(tr0);
        }

        double pc = getBarSeries().getBar(index - 1).getClosePrice().doubleValue();

        double tr = Math.max(
                h - l,
                Math.max(Math.abs(h - pc), Math.abs(l - pc))
        );

        if (!Double.isFinite(tr) || tr < 0) tr = 0.0;
        return getBarSeries().numFactory().numOf(tr);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 1;
    }
}
