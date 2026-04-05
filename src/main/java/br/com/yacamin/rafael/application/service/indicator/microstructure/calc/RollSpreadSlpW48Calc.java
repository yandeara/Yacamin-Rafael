package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.roll.extension.RollSlopeExtension;
import org.springframework.stereotype.Component;

@Component
public class RollSpreadSlpW48Calc implements DescribableCalc {
    public static double calculate(RollSlopeExtension slope, int index) { return slope.getValue(index).doubleValue(); }
    @Override public FeatureDescription describe() { return new FeatureDescription("mic_roll_spread_slp_w48", "Roll Spread Slope W48", "microstructure", "slope(spread(w=48), 20)", "Slope do spread de Roll (janela 48).", "unbounded", "=0.001"); }
}
