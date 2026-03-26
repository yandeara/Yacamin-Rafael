package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

// =========================================================================
// Helper Indicator: width = (upper - lower) / middle
// =========================================================================
public class BollingerWidthIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> up;
    private final Indicator<Num> low;
    private final Indicator<Num> mid;

    public BollingerWidthIndicator(BarSeries series,
                                    Indicator<Num> up,
                                    Indicator<Num> low,
                                    Indicator<Num> mid) {
        super(series);
        this.up = up;
        this.low = low;
        this.mid = mid;
    }

    @Override
    protected Num calculate(int index) {
        Num width = up.getValue(index).minus(low.getValue(index));
        return width.dividedBy(mid.getValue(index));
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }

}
