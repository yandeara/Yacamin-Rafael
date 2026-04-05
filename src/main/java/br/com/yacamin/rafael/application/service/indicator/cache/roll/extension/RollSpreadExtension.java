package br.com.yacamin.rafael.application.service.indicator.cache.roll.extension;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Roll Spread: estimativa do bid-ask spread a partir da covariancia.
 * spread = 2 * sqrt(-cov) quando cov < 0, senao 0.
 * Covariancia positiva invalida a premissa do modelo Roll.
 */
public class RollSpreadExtension extends CachedIndicator<Num> {

    private final RollCovExtension cov;

    public RollSpreadExtension(RollCovExtension cov) {
        super(cov.getBarSeries());
        this.cov = cov;
    }

    @Override
    protected Num calculate(int index) {
        double covVal = cov.getValue(index).doubleValue();
        if (covVal >= 0) {
            return getBarSeries().numFactory().numOf(0);
        }
        return getBarSeries().numFactory().numOf(2.0 * Math.sqrt(-covVal));
    }

    @Override
    public int getCountOfUnstableBars() {
        return cov.getCountOfUnstableBars();
    }
}
