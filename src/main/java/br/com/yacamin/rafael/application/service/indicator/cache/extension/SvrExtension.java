package br.com.yacamin.rafael.application.service.indicator.cache.extension;

import br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Signed Volume Ratio (SVR) = (takerBuy - takerSell) / (takerBuy + takerSell)
 */
public class SvrExtension extends CachedIndicator<Num> {

    public SvrExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        MikhaelBar mBar = (MikhaelBar) getBarSeries().getBar(index);
        double buy = mBar.getTakerBuyBaseVolume().doubleValue();
        double sell = mBar.getTakerSellBaseVolume().doubleValue();
        double denom = buy + sell;

        if (denom < 1e-12) {
            return getBarSeries().numFactory().numOf(0);
        }

        return getBarSeries().numFactory().numOf((buy - sell) / denom);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
