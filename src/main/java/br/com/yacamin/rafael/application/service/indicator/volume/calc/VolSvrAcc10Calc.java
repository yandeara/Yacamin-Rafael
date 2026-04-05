package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
@Component
public class VolSvrAcc10Calc implements DescribableCalc {
    public static double calculate(BarSeries series, int index) {
        return VolSvrCalc.svr(series, index) - VolSvrCalc.svr(series, index - 10);
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_svr_acc_10", "SVR Acc 10", "volume", "svr(t)-svr(t-10)", "Aceleracao SVR 10.", "unbounded", ""); }
}
