package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolSvrAcc5Calc implements DescribableCalc {
    public static double calculate(BarSeries series, int index) {
        return VolSvrCalc.svr(series, index) - VolSvrCalc.svr(series, index - 5);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_svr_acc_5", "SVR Acc 5", "volume", "svr(t)-svr(t-5)", "Aceleracao SVR 5.", "unbounded", ""); }
}
