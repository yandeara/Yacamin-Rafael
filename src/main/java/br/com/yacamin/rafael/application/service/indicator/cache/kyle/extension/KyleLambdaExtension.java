package br.com.yacamin.rafael.application.service.indicator.cache.kyle.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Kyle Lambda: regressao return ~ OFI sobre uma janela.
 * lambda = sum(ret * ofi) / sum(ofi^2)
 * Nota: usa retorno com sinal (nao absoluto).
 */
public class KyleLambdaExtension extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> close;
    private final Indicator<Num> ofi;
    private final int window;

    public KyleLambdaExtension(Indicator<Num> close, Indicator<Num> ofi, int window) {
        super(close.getBarSeries());
        this.close = close;
        this.ofi = ofi;
        this.window = window;
    }

    @Override
    protected Num calculate(int index) {
        if (index < window) {
            return getBarSeries().numFactory().numOf(0);
        }

        double sumXY = 0;
        double sumXX = 0;

        for (int i = 0; i < window; i++) {
            int idx = index - i;
            if (idx <= 0) {
                continue;
            }

            double c1 = close.getValue(idx).doubleValue();
            double c0 = close.getValue(idx - 1).doubleValue();

            if (Math.abs(c0) < EPS) {
                continue;
            }

            double ret = (c1 - c0) / c0;
            double ofiVal = ofi.getValue(idx).doubleValue();

            sumXY += ret * ofiVal;
            sumXX += ofiVal * ofiVal;
        }

        if (sumXX < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        return getBarSeries().numFactory().numOf(sumXY / sumXX);
    }

    @Override
    public int getCountOfUnstableBars() {
        return Math.max(1, window);
    }
}
