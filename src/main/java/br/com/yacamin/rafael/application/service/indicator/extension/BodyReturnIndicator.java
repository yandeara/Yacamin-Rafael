package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class BodyReturnIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    private final Indicator<Num> body;
    private final Indicator<Num> close;

    public BodyReturnIndicator(Indicator<Num> body, Indicator<Num> close) {
        super(body.getBarSeries());
        this.body = body;
        this.close = close;
    }

    @Override
    protected Num calculate(int index) {
        if (index <= 0) return getBarSeries().numFactory().numOf(0);

        double prevClose = close.getValue(index - 1).doubleValue();
        if (!Double.isFinite(prevClose) || Math.abs(prevClose) < EPS) return getBarSeries().numFactory().numOf(0);

        double v = body.getValue(index).doubleValue() / (prevClose + EPS);
        if (!Double.isFinite(v)) v = 0.0;

        return getBarSeries().numFactory().numOf(v);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 1;
    }
}
