package br.com.yacamin.rafael.application.service.indicator.extension;

import br.com.yacamin.rafael.domain.RafaelBar;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Signed Volume Ratio (SVR):
 * (taker_buy_base - taker_sell_base) / (taker_buy_base + taker_sell_base + eps)
 *
 * Obs: lê dados brutos do RafaelBar (taker volumes), não campos derivados.
 */
public class SvrIndicator extends CachedIndicator<Num> {

    private static final double EPS = 1e-12;

    public SvrIndicator(org.ta4j.core.BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        RafaelBar bar = (RafaelBar) getBarSeries().getBar(index);

        double buy  = bar.getTakerBuyBaseVolume().doubleValue();
        double sell = bar.getTakerSellBaseVolume().doubleValue();

        double denom = buy + sell;
        if (!Double.isFinite(denom) || denom < EPS) {
            return getBarSeries().numFactory().numOf(0);
        }

        double svr = (buy - sell) / (denom + EPS);
        if (!Double.isFinite(svr)) svr = 0.0;

        return getBarSeries().numFactory().numOf(svr);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
