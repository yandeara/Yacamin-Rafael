package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.extension.OfiExtension;
import org.springframework.stereotype.Component;
@Component
public class VolOfiCalc implements DescribableCalc {
    public static double calculate(OfiExtension ofi, int index) { return ofi.getValue(index).doubleValue(); }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_ofi", "OFI Raw", "volume", "takerBuy - takerSell", "Order Flow Imbalance bruto.", "unbounded", "buy=500 sell=300 -> 200"); }
}
