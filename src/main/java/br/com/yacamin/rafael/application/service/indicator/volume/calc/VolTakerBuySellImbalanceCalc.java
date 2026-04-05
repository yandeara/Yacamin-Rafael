package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolTakerBuySellImbalanceCalc implements DescribableCalc {
    static double imb(BarSeries s, int i) {
        MikhaelBar b = (MikhaelBar) s.getBar(i);
        double tb = b.getTakerBuyQuoteVolume().doubleValue();
        double ts = b.getTakerSellQuoteVolume().doubleValue();
        return (tb - ts) / (tb + ts + 1e-12);
    }
    public static double calculate(BarSeries series, int index) { return imb(series, index); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_taker_buy_sell_imbalance", "Taker Imbalance", "volume", "(buy-sell)/(buy+sell)", "Imbalance taker.", "-1..1", ""); }
}
