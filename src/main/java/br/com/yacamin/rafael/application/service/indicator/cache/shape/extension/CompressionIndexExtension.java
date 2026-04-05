package br.com.yacamin.rafael.application.service.indicator.cache.shape.extension;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Indice de compressao do candle: quanto menor o body relativo ao range, maior a compressao.
 * compressionIndex = 1 - |close - open| / range
 */
public class CompressionIndexExtension extends CachedIndicator<Num> {

    public CompressionIndexExtension(BarSeries series) {
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
            return getBarSeries().numFactory().numOf(1.0);
        }

        return getBarSeries().numFactory().numOf(1.0 - Math.abs(c - o) / range);
    }

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }
}
