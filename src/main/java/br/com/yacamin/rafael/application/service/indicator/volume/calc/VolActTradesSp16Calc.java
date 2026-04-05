package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolActTradesSp16Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int end = series.getEndIndex(); int k = 16; int start = end - k + 1;
        int score = 0;
        for (int i = start + 1; i <= end; i++) {
            double now = series.getBar(i).getTrades();
            double prev = series.getBar(i - 1).getTrades();
            score += (now - prev) > 0 ? 1 : ((now - prev) < 0 ? -1 : 0);
        }
        return (double) score / (double) k;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_act_trades_sp_16", "Trades SP 16", "volume", "sustainedPressure(trades, 16)", "Pressao sustentada trades janela 16.", "-1..1", ""); }
}
