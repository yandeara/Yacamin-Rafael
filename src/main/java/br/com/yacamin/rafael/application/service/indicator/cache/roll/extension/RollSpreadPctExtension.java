package br.com.yacamin.rafael.application.service.indicator.cache.roll.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Roll Spread Pct: estimativa do bid-ask spread a partir da covariancia percentual.
 * spreadPct = 2 * sqrt(-covPct) quando covPct < 0, senao 0.
 */
public class RollSpreadPctExtension extends CachedIndicator<Num> {

    private final RollCovPctExtension covPct;

    public RollSpreadPctExtension(RollCovPctExtension covPct) {
        super(covPct.getBarSeries());
        this.covPct = covPct;
    }

    @Override
    protected Num calculate(int index) {
        double c = covPct.getValue(index).doubleValue();
        if (c >= 0) {
            return getBarSeries().numFactory().numOf(0);
        }
        return getBarSeries().numFactory().numOf(2.0 * Math.sqrt(-c));
    }

    @Override
    public int getCountOfUnstableBars() {
        return covPct.getCountOfUnstableBars();
    }
}
