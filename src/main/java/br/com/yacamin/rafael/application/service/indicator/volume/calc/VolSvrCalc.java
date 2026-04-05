package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolSvrCalc implements DescribableCalc {
    static double svr(BarSeries s, int i) {
        MikhaelBar b = (MikhaelBar) s.getBar(i);
        double buy = b.getTakerBuyBaseVolume().doubleValue();
        double sell = b.getTakerSellBaseVolume().doubleValue();
        return (buy - sell) / (buy + sell);
    }
    public static double calculate(BarSeries series, int index) { return svr(series, index); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_svr", "SVR", "volume", "(buy-sell)/(buy+sell)", "Signed volume ratio.", "-1..1", ""); }
}
