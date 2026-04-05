package br.com.yacamin.rafael.application.service.indicator.cache.amihud.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Slope (regressao linear) do Amihud ILLIQ sobre uma janela.
 * Calcula o coeficiente angular (beta) de OLS: y = amihud values, x = 0..window-1
 * beta = (n*sum(x*y) - sum(x)*sum(y)) / (n*sum(x^2) - sum(x)^2)
 */
public class AmihudSlopeExtension extends CachedIndicator<Num> {

    private final Indicator<Num> source;
    private final int window;

    public AmihudSlopeExtension(Indicator<Num> source, int window) {
        super(source.getBarSeries());
        this.source = source;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (index < window - 1) {
            return getBarSeries().numFactory().numOf(0);
        }

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < window; i++) {
            double x = i;
            double y = source.getValue(index - window + 1 + i).doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denom = window * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-12) {
            return getBarSeries().numFactory().numOf(0);
        }

        double slope = (window * sumXY - sumX * sumY) / denom;
        return getBarSeries().numFactory().numOf(slope);
    }

    @Override
    public int getCountOfUnstableBars() {
        return window;
    }
}
