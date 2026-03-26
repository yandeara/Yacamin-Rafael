package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class DeltaIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> base;

    public DeltaIndicator(BarSeries series, Indicator<Num> base) {
        super(series);
        this.base = base;
    }

    @Override
    protected Num calculate(int index) {

        // primeira barra: não há variação anterior → delta = 0
        if (index == 0) {
            return getBarSeries().numFactory().zero();
        }

        Num current  = base.getValue(index);
        Num previous = base.getValue(index - 1);

        return current.minus(previous);
    }

    @Override
    public int getCountOfUnstableBars() {
        // 1 barra "instável" por definição do delta (precisa da anterior)
        return 1;
    }
}
