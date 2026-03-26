package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

// RV = sqrt( Σ ln(Ct/Ct-1)^2 ) over period
public class RealizedVolIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> close;
    private final int period;

    public RealizedVolIndicator(Indicator<Num> close, int period) {
        super(close.getBarSeries());
        this.close = close;
        this.period = period;
    }

    @Override
    protected Num calculate(int index) {
        if (index <= 0) {
            return getBarSeries().numFactory().numOf(0);
        }

        int start = Math.max(1, index - period + 1);
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double c = close.getValue(i).doubleValue();
            double p = close.getValue(i - 1).doubleValue();
            double r = Math.log(c / p);
            sumSq += r * r;
        }

        return getBarSeries().numFactory().numOf(Math.sqrt(sumSq));
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
