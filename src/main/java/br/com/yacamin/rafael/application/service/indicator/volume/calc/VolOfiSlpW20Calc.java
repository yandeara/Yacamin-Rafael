package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.extension.OfiExtension;
import org.springframework.stereotype.Component;
@Component
public class VolOfiSlpW20Calc implements DescribableCalc {
    public static double calculate(OfiExtension ofi, int index) {
        if (index < 19) return 0;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < 20; i++) { double x = i; double y = ofi.getValue(index - 19 + i).doubleValue(); sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x; }
        double denom = 20 * sumX2 - sumX * sumX;
        return (Math.abs(denom) < 1e-12) ? 0 : (20 * sumXY - sumX * sumY) / denom;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_ofi_slp_w20", "OFI Slope W20", "volume", "linreg_slope(ofi, 20)", "Slope do OFI nos ultimos 20 candles.", "unbounded", "ofi subindo -> slope positivo"); }
}
