package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.num.Num;

public class TsiIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> close;
    private final int longPeriod;
    private final int shortPeriod;

    private final EMAIndicator pcEma1;
    private final EMAIndicator pcEma2;
    private final EMAIndicator absPcEma1;
    private final EMAIndicator absPcEma2;

    private static class PriceChangeIndicator extends CachedIndicator<Num> {

        private final Indicator<Num> price;

        public PriceChangeIndicator(BarSeries series, Indicator<Num> price) {
            super(series);
            this.price = price;
        }

        @Override
        protected Num calculate(int index) {
            var factory = getBarSeries().numFactory();
            if (index == 0) {
                return factory.zero();
            }
            Num curr = price.getValue(index);
            Num prev = price.getValue(index - 1);
            return curr.minus(prev);
        }

        @Override
        public int getCountOfUnstableBars() {
            return price.getCountOfUnstableBars() + 1;
        }
    }

    private static class AbsIndicator extends CachedIndicator<Num> {

        private final Indicator<Num> base;

        public AbsIndicator(BarSeries series, Indicator<Num> base) {
            super(series);
            this.base = base;
        }

        @Override
        protected Num calculate(int index) {
            Num v = base.getValue(index);
            return v.abs();
        }

        @Override
        public int getCountOfUnstableBars() {
            return base.getCountOfUnstableBars();
        }
    }

    public TsiIndicator(BarSeries series, Indicator<Num> close, int longPeriod, int shortPeriod) {
        super(series);
        this.close = close;
        this.longPeriod = longPeriod;
        this.shortPeriod = shortPeriod;

        PriceChangeIndicator pc = new PriceChangeIndicator(series, close);
        AbsIndicator absPc = new AbsIndicator(series, pc);

        EMAIndicator pcLong = new EMAIndicator(pc, longPeriod);
        this.pcEma2 = new EMAIndicator(pcLong, shortPeriod);

        EMAIndicator absPcLong = new EMAIndicator(absPc, longPeriod);
        this.absPcEma2 = new EMAIndicator(absPcLong, shortPeriod);

        this.pcEma1 = pcLong;
        this.absPcEma1 = absPcLong;
    }

    @Override
    protected Num calculate(int index) {
        var factory = getBarSeries().numFactory();

        Num num = pcEma2.getValue(index);
        Num den = absPcEma2.getValue(index);

        if (den.isZero()) {
            return factory.zero();
        }

        return num.dividedBy(den).multipliedBy(factory.numOf(100));
    }

    @Override
    public int getCountOfUnstableBars() {
        return absPcEma2.getCountOfUnstableBars();
    }
}
