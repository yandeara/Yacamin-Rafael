package br.com.yacamin.rafael.application.service.indicator.cache.shape.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Indice de forma direcional do candle: body ratio com sinal da direcao.
 * shapeIndex = (|close - open| / range) * signum(close - open)
 */
public class ShapeIndexExtension extends CachedIndicator<Num> {

    public ShapeIndexExtension(BarSeries series) {
        super(series);
    }

    @Override
    protected Num calculate(int index) {
        Bar bar = getBarSeries().getBar(index);
        double c = bar.getClosePrice().doubleValue();
        double o = bar.getOpenPrice().doubleValue();
        double h = bar.getHighPrice().doubleValue();
        double l = bar.getLowPrice().doubleValue();
        double range = h - l;

        if (range < 1e-12) {
            return getBarSeries().numFactory().numOf(0);
        }

        double bodyRatio = Math.abs(c - o) / range;
        double direction = Math.signum(c - o);

        return getBarSeries().numFactory().numOf(bodyRatio * direction);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
