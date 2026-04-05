package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolActQuoteAcc16Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int end = series.getEndIndex(); int k = 16; int start = end - k + 1;
        double acc = 0.0;
        for (int i = start + 1; i <= end; i++) {
            double now = ((br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar) series.getBar(i)).getQuoteVolume().doubleValue();
            double prev = ((br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar) series.getBar(i - 1)).getQuoteVolume().doubleValue();
            acc += now - prev;
        }
        return acc;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_act_quote_acc_16", "Quote Acc 16", "volume", "pressureAcc(quote, 16)", "Acumulacao pressao quote janela 16.", "unbounded", ""); }
}
