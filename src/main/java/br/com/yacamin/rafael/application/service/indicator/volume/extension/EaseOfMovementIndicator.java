package br.com.yacamin.rafael.application.service.indicator.volume.extension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.Num;

public class EaseOfMovementIndicator extends CachedIndicator<Num> {

    private final HighPriceIndicator high;
    private final LowPriceIndicator low;
    private final VolumeIndicator volume;
    private final Num two;
    private final Num zero;

    public EaseOfMovementIndicator(BarSeries series) {
        super(series);
        this.high = new HighPriceIndicator(series);
        this.low = new LowPriceIndicator(series);
        this.volume = new VolumeIndicator(series);
        this.two = series.numFactory().numOf(2);
        this.zero = series.numFactory().zero();
    }

    @Override
    protected Num calculate(int index) {

        if (index == 0) {
            return zero;
        }

        Num highToday = high.getValue(index);
        Num lowToday  = low.getValue(index);
        Num highPrev  = high.getValue(index - 1);
        Num lowPrev   = low.getValue(index - 1);
        Num vol       = volume.getValue(index);

        Num midToday = highToday.plus(lowToday).dividedBy(two);
        Num midPrev  = highPrev.plus(lowPrev).dividedBy(two);
        Num distanceMoved = midToday.minus(midPrev);

        Num range = highToday.minus(lowToday);

        if (range.isZero() || vol.isZero()) {
            return zero;
        }

        return distanceMoved
                .multipliedBy(range)
                .dividedBy(vol);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 1;
    }
}
