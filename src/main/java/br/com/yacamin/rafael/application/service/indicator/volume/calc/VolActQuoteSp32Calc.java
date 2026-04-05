package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolActQuoteSp32Calc implements DescribableCalc {
    public static double calculate(BarSeries series) {
        int end = series.getEndIndex(); int k = 32; int start = end - k + 1;
        int score = 0;
        for (int i = start + 1; i <= end; i++) {
            double now = ((br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar) series.getBar(i)).getQuoteVolume().doubleValue();
            double prev = ((br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar) series.getBar(i - 1)).getQuoteVolume().doubleValue();
            score += (now - prev) > 0 ? 1 : ((now - prev) < 0 ? -1 : 0);
        }
        return (double) score / (double) k;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_act_quote_sp_32", "Quote SP 32", "volume", "sustainedPressure(quote, 32)", "Pressao sustentada quote janela 32.", "-1..1", ""); }
}
