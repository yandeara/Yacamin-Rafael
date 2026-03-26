package br.com.yacamin.rafael.application.service.indicator.volume.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.num.Num;

public class EomSmoothedIndicator extends CachedIndicator<Num> {

    private final SMAIndicator sma;

    public EomSmoothedIndicator(BarSeries series, int period) {
        super(series);
        EaseOfMovementIndicator raw = new EaseOfMovementIndicator(series);
        this.sma = new SMAIndicator(raw, period);
    }

    @Override
    protected Num calculate(int index) {
        return sma.getValue(index);
    }

    @Override
    public int getCountOfUnstableBars() {
        return sma.getCountOfUnstableBars();
    }
}
