package br.com.yacamin.rafael.application.service.indicator.extension;

import br.com.yacamin.rafael.domain.RafaelBar;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * OFI raw = taker_buy_base_volume - taker_sell_base_volume
 */
public class OfiIndicator extends CachedIndicator<Num> {

    public OfiIndicator(org.ta4j.core.BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        var bar = (RafaelBar) getBarSeries().getBar(index);

        double buy  = bar.getTakerBuyBaseVolume().doubleValue();
        double sell = bar.getTakerSellBaseVolume().doubleValue();

        return getBarSeries().numFactory().numOf(buy - sell);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
