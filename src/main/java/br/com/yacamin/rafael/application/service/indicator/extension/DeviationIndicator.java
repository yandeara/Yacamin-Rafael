package br.com.yacamin.rafael.application.service.indicator.extension;

import java.math.BigDecimal;
import java.math.MathContext;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.num.Num;

public class DeviationIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> base;
    private final EMAIndicator ema;

    public DeviationIndicator(Indicator<Num> base, Integer period) {
        super(base.getBarSeries());
        this.base = base;
        this.ema = new EMAIndicator(base, period);
    }

    @Override
    protected Num calculate(int index) {
        Num current = base.getValue(index);
        Num emaValue = ema.getValue(index);

        if (current == null || emaValue == null) {
            return getBarSeries().numFactory().zero();
        }

        BigDecimal emaBd = emaValue.bigDecimalValue();
        if (emaBd.compareTo(BigDecimal.ZERO) == 0) {
            return getBarSeries().numFactory().zero();
        }

        BigDecimal result = current
                .bigDecimalValue()
                .divide(emaBd, MathContext.DECIMAL64);

        return getBarSeries().numFactory().numOf(result);
    }

    @Override
    public int getCountOfUnstableBars() {
        return base.getCountOfUnstableBars();
    }
}
