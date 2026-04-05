package br.com.yacamin.rafael.application.service.indicator.cache.roll.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Roll Covariance Pct: media de (ΔP%_t × ΔP%_{t-1}) sobre uma janela.
 * ΔP%_t = (close[t] - close[t-1]) / close[t-1]
 * Versao percentual da covariancia de Roll — scale-invariant.
 */
public class RollCovPctExtension extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;
    private final Indicator<Num> close;
    private final int window;

    public RollCovPctExtension(Indicator<Num> close, int window) {
        super(close.getBarSeries());
        this.close = close;
        this.window = window;
    }

    private double deltaPct(int idx) {
        if (idx <= 0) return 0.0;
        double c1 = close.getValue(idx).doubleValue();
        double c0 = close.getValue(idx - 1).doubleValue();
        if (Math.abs(c0) < EPS) return 0.0;
        return (c1 - c0) / c0;
    }

    @Override
    protected Num calculate(int index) {
        if (index < window + 1) {
            return getBarSeries().numFactory().numOf(0);
        }

        double sum = 0;
        for (int i = 0; i < window; i++) {
            int idx = index - i;
            sum += deltaPct(idx) * deltaPct(idx - 1);
        }

        return getBarSeries().numFactory().numOf(sum / window);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window + 1;
    }
}
