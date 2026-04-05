package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
@Component
public class VltRangeAtr14LocChgCalc implements DescribableCalc {
    private static double trueRange(BarSeries s, int i) { double h = s.getBar(i).getHighPrice().doubleValue(); double l = s.getBar(i).getLowPrice().doubleValue(); if (i == 0) return Math.abs(h - l); double pc = s.getBar(i - 1).getClosePrice().doubleValue(); return Math.max(h - l, Math.max(Math.abs(h - pc), Math.abs(l - pc))); }
    public static double calculate(BarSeries series, ATRIndicator atr, int index) { double locNow = trueRange(series, index) / atr.getValue(index).doubleValue(); double locPrev = trueRange(series, index - 1) / atr.getValue(index - 1).doubleValue(); double r = (locNow / locPrev) - 1.0; return (Double.isInfinite(r) || Double.isNaN(r)) ? 0 : r; }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_range_atr_14_loc_chg", "Range/ATR14 Local Chg", "volatility", "(loc[t]/loc[t-1])-1", "Variacao do local ATR14.", "unbounded", ""); }
}
