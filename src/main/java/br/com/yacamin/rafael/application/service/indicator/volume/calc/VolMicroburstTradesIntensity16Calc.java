package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolMicroburstTradesIntensity16Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int idx = series.getEndIndex(); int p = 16; int start = idx - p + 1;
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += series.getBar(i).getTrades();
        double mean = sum / p;
        return series.getBar(idx).getTrades() / mean;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_microburst_trades_intensity_16", "Microburst Trades Intensity 16", "volume", "trades/mean(trades)", "Intensidade trades janela 16.", "0+", ""); }
}
