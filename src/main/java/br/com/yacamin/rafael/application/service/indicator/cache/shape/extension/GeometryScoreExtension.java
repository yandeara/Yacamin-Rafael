package br.com.yacamin.rafael.application.service.indicator.cache.shape.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Score geometrico do candle combinando body ratio, simetria de pavios e centralidade do close.
 * geometryScore = 0.4 * bodyRatio + 0.3 * symmetry + 0.3 * centrality
 */
public class GeometryScoreExtension extends CachedIndicator<Num> {

    public GeometryScoreExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        double o = bar.getOpenPrice().doubleValue();
        double c = bar.getClosePrice().doubleValue();
        double h = bar.getHighPrice().doubleValue();
        double l = bar.getLowPrice().doubleValue();
        double range = h - l;

        if (range < 1e-12) {
            return getBarSeries().numFactory().numOf(0);
        }

        double bodyRatio = Math.abs(c - o) / range;
        double upperWick = h - Math.max(o, c);
        double lowerWick = Math.min(o, c) - l;
        double totalWick = upperWick + lowerWick;
        double symmetry = (totalWick < 1e-12) ? 1.0 : 1.0 - Math.abs(upperWick - lowerWick) / totalWick;
        double closePos = (c - l) / range;
        double centrality = 1.0 - 2.0 * Math.abs(closePos - 0.5);

        return getBarSeries().numFactory().numOf(0.4 * bodyRatio + 0.3 * symmetry + 0.3 * centrality);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
