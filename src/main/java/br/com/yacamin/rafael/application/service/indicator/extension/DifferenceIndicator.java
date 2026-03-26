package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/*
* Difference Indicator
* Pode ser usado para Acceleração de Slope ou pra qualquer calculo de Diferença
*  */
public class DifferenceIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> base;

    public DifferenceIndicator(Indicator<Num> base) {
        super(base.getBarSeries());
        this.base = base;
    }

    @Override
    protected Num calculate(int index) {

        if (index < 1) {
            return getBarSeries()
                    .numFactory()
                    .numOf(0);
        }

        Num current = base.getValue(index);
        Num previous = base.getValue(index - 1);

        return current.minus(previous);
    }

    @Override
    public int getCountOfUnstableBars() {
        return base.getCountOfUnstableBars() + 1;
    }
}
