package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Roll Spread Pct = 2 * sqrt(-covPct) if covPct < 0 else 0
 */
public class RollSpreadPctIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> covPct;

    public RollSpreadPctIndicator(Indicator<Num> covPct) {
        super(covPct.getBarSeries());
        this.covPct = covPct;
    }

    @Override
    protected Num calculate(int index) {
        double c = covPct.getValue(index).doubleValue();

        double spreadPct;
        if (!Double.isFinite(c) || c >= 0.0) spreadPct = 0.0;
        else spreadPct = 2.0 * Math.sqrt(-c);

        return getBarSeries().numFactory().numOf(spreadPct);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
