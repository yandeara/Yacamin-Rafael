package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolTakerBuyRatioRel16Calc implements DescribableCalc {
    public static double calculate(BarSeries s, int idx) {
        int w = 16; int start = Math.max(0, idx - w + 1); int n = idx - start + 1;
        double sum = 0; for (int i = start; i <= idx; i++) sum += VolTakerBuyRatioCalc.tbr(s, i);
        return VolTakerBuyRatioCalc.tbr(s, idx) / (sum / n);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_taker_buy_ratio_rel_16", "TBR Rel 16", "volume", "tbr/mean(tbr,16)", "TBR relativo janela 16.", "0+", ""); }
}
