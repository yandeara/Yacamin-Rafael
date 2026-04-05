package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.extension.MikhaelBar;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolTakerBuyRatioCalc implements DescribableCalc {
    static double tbr(BarSeries s, int i) {
        MikhaelBar b = (MikhaelBar) s.getBar(i);
        double qv = b.getQuoteVolume().doubleValue();
        double tb = b.getTakerBuyQuoteVolume().doubleValue();
        double r = tb / qv;
        if (Double.isNaN(r) || Double.isInfinite(r)) return 0;
        return r;
    }
    public static double calculate(BarSeries series, int index) { return tbr(series, index); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_taker_buy_ratio", "Taker Buy Ratio", "volume", "takerBuyQuoteVol/quoteVol", "Razao taker buy.", "0..1", ""); }
}
