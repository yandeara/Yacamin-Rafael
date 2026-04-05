package br.com.yacamin.rafael.application.service.indicator.volatility.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
@Component
public class VltRangeAtr14LocCalc implements DescribableCalc {
    public static double calculate(BarSeries series, ATRIndicator atr, int index) { double h = series.getBar(index).getHighPrice().doubleValue(); double l = series.getBar(index).getLowPrice().doubleValue(); double tr; if (index == 0) { tr = Math.abs(h - l); } else { double pc = series.getBar(index - 1).getClosePrice().doubleValue(); tr = Math.max(h - l, Math.max(Math.abs(h - pc), Math.abs(l - pc))); } return tr / atr.getValue(index).doubleValue(); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vlt_range_atr_14_loc", "Range/ATR14 Local", "volatility", "TR/ATR14", "True Range normalizado pelo ATR14.", "0+", ""); }
}
