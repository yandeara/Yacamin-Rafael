package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolTradesSpikeScore16Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int idx = series.getEndIndex(); int p = 16; int start = idx - p + 1;
        double sum = 0, sumSq = 0;
        for (int i = start; i <= idx; i++) { double v = series.getBar(i).getTrades(); sum += v; sumSq += v * v; }
        double mean = sum / p; double var = (sumSq / p) - (mean * mean);
        if (var < 0 && var > -1e-9) var = 0.0;
        double std = Math.sqrt(var);
        return (series.getBar(idx).getTrades() - mean) / std;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_trades_spike_score_16", "Trades Spike Score 16", "volume", "zscore(trades, 16)", "Z-score trades janela 16.", "unbounded", ""); }
}
