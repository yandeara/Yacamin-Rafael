package br.com.yacamin.rafael.application.service.indicator.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.TypicalPriceIndicator;
import org.ta4j.core.num.Num;

public class MFIIndicator extends CachedIndicator<Num> {

    private final int timeFrame;
    private final BarSeries series;
    private final TypicalPriceIndicator tp;

    public MFIIndicator(BarSeries series, int timeFrame) {
        super(series);
        this.series = series;
        this.timeFrame = timeFrame;
        this.tp = new TypicalPriceIndicator(series);
    }

    @Override
    protected Num calculate(int index) {

        if (index < 1) {
            return getBarSeries().numFactory().numOf(0);
        }

        Num positiveFlow = getBarSeries().numFactory().numOf(0);
        Num negativeFlow = getBarSeries().numFactory().numOf(0);

        for (int i = Math.max(1, index - timeFrame + 1); i <= index; i++) {
            Num currentTP = tp.getValue(i);
            Num prevTP = tp.getValue(i - 1);

            Num moneyFlow = currentTP.multipliedBy(series.getBar(i).getVolume());

            if (currentTP.isGreaterThan(prevTP)) {
                positiveFlow = positiveFlow.plus(moneyFlow);
            } else if (currentTP.isLessThan(prevTP)) {
                negativeFlow = negativeFlow.plus(moneyFlow);
            }
        }

        if (negativeFlow.isZero()) {
            // MFI máximo (pressão compradora total)
            return getBarSeries().numFactory().numOf(100);
        }

        Num moneyFlowRatio = positiveFlow.dividedBy(negativeFlow);
        Num rawMFI = getBarSeries().numFactory().numOf(100)
                .minus(getBarSeries().numFactory().numOf(100).dividedBy(moneyFlowRatio.plus(getBarSeries().numFactory().numOf(1))));

        return rawMFI;
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}