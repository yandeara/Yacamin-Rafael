package br.com.yacamin.rafael.application.service.indicator.cache.body.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Body energy = |body| * (high - low).
 */
public class BodyEnergyExtension extends CachedIndicator<Num> {

    private final Indicator<Num> source;

    public BodyEnergyExtension(Indicator<Num> source) {
        super(source.getBarSeries());
        this.source = source;
    }

    @Override
    protected Num calculate(int index) {
        double absBody = source.getValue(index).doubleValue();
        Bar bar = getBarSeries().getBar(index);
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        return getBarSeries().numFactory().numOf(absBody * (high - low));
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
