package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolActTradesAcc16Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int end = series.getEndIndex(); int k = 16; int start = end - k + 1;
        double acc = 0.0;
        for (int i = start + 1; i <= end; i++) {
            double now = series.getBar(i).getTrades();
            double prev = series.getBar(i - 1).getTrades();
            acc += now - prev;
        }
        return acc;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_act_trades_acc_16", "Trades Acc 16", "volume", "pressureAcc(trades, 16)", "Acumulacao pressao trades janela 16.", "unbounded", ""); }
}
