package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.num.Num;

public class TrixIndicator extends CachedIndicator<Num> {

    private final EMAIndicator ema1;
    private final EMAIndicator ema2;
    private final EMAIndicator ema3;
    private final int period;

    public TrixIndicator(BarSeries series, Indicator<Num> base, int period) {
        super(series);
        this.period = period;
        this.ema1 = new EMAIndicator(base, period);
        this.ema2 = new EMAIndicator(ema1, period);
        this.ema3 = new EMAIndicator(ema2, period);
    }

    @Override
    protected Num calculate(int index) {
        var factory = getBarSeries().numFactory();

        if (index == 0) {
            return factory.zero();
        }

        Num prev = ema3.getValue(index - 1);
        if (prev.isZero()) {
            return factory.zero();
        }

        Num curr = ema3.getValue(index);
        return curr.minus(prev)
                .dividedBy(prev)
                .multipliedBy(factory.numOf(100));
    }

    @Override
    public int getCountOfUnstableBars() {
        // podemos delegar pro último nível suavizado, que já incorpora o período
        return ema3.getCountOfUnstableBars();
    }
}
