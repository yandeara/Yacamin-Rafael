package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class LinearRegressionSlopeIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> base;
    private final int period;

    public LinearRegressionSlopeIndicator(BarSeries series, Indicator<Num> base, int period) {
        super(series);
        this.base = base;
        this.period = period;
    }

    @Override
    protected Num calculate(int index) {

        var factory = getBarSeries().numFactory();

        // precisa de 'period' barras completas → primeira barra estável em (period - 1)
        if (index < period - 1) {
            return factory.zero();
        }

        int start = index - period + 1;

        Num zero = factory.zero();
        Num sumX  = zero;
        Num sumY  = zero;
        Num sumXY = zero;
        Num sumX2 = zero;

        for (int i = 0; i < period; i++) {
            Num x = factory.numOf(i);
            Num y = base.getValue(start + i);

            sumX  = sumX.plus(x);
            sumY  = sumY.plus(y);
            sumXY = sumXY.plus(x.multipliedBy(y));
            sumX2 = sumX2.plus(x.multipliedBy(x));
        }

        Num w = factory.numOf(period);

        Num numerator   = w.multipliedBy(sumXY).minus(sumX.multipliedBy(sumY));
        Num denominator = w.multipliedBy(sumX2).minus(sumX.multipliedBy(sumX));

        if (denominator.isZero()) {
            return factory.zero();
        }

        return numerator.dividedBy(denominator);
    }

    @Override
    public int getCountOfUnstableBars() {
        // barras instáveis do indicador base + (period - 1) necessárias para a janela completa
        return base.getCountOfUnstableBars() + period - 1;
    }
}
