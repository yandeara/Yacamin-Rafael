package br.com.yacamin.rafael.application.service.indicator.cache.extension;

import br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Order Flow Imbalance (OFI) = takerBuyBaseVolume - takerSellBaseVolume
 */
public class OfiExtension extends CachedIndicator<Num> {

    public OfiExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        MikhaelBar mBar = (MikhaelBar) getBarSeries().getBar(index);
        double buy = mBar.getTakerBuyBaseVolume().doubleValue();
        double sell = mBar.getTakerSellBaseVolume().doubleValue();

        return getBarSeries().numFactory().numOf(buy - sell);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
