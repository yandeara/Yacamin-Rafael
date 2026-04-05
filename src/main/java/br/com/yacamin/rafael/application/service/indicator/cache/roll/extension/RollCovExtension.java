package br.com.yacamin.rafael.application.service.indicator.cache.roll.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Roll Covariance: media de (ΔP_t × ΔP_{t-1}) sobre uma janela.
 * ΔP_t = close[t] - close[t-1]
 * Covariancia negativa indica presenca de bid-ask bounce (Roll, 1984).
 */
public class RollCovExtension extends CachedIndicator<Num> {

    private final Indicator<Num> close;
    private final int window;

    public RollCovExtension(Indicator<Num> close, int window) {
        super(close.getBarSeries());
        this.close = close;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (index < window + 1) {
            return getBarSeries().numFactory().numOf(0);
        }

        double sum = 0;
        for (int i = 0; i < window; i++) {
            int idx = index - i;
            double dp1 = close.getValue(idx).doubleValue() - close.getValue(idx - 1).doubleValue();
            double dp0 = close.getValue(idx - 1).doubleValue() - close.getValue(idx - 2).doubleValue();
            sum += dp1 * dp0;
        }

        return getBarSeries().numFactory().numOf(sum / window);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window + 1;
    }
}
