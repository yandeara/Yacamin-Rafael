package br.com.yacamin.rafael.application.service.indicator.volume.calc;
import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.extension.OfiExtension;
import org.springframework.stereotype.Component;
@Component
public class VolOfiRel16Calc implements DescribableCalc {
    public static double calculate(OfiExtension ofi, int index) {
        if (index < 16 - 1) return 0;
        int start = Math.max(0, index - 16 + 1);
        double sumSq = 0;
        int n = 0;
        for (int i = start; i <= index; i++) { double x = ofi.getValue(i).doubleValue(); sumSq += x * x; n++; }
        if (n == 0) return 0;
        double rms = Math.sqrt(sumSq / n);
        return (rms < 1e-12) ? 0 : ofi.getValue(index).doubleValue() / rms;
    }
    @Override public FeatureDescription describe() { return new FeatureDescription("vol_ofi_rel_16", "OFI Relative 16", "volume", "ofi / RMS(ofi, 16)", "OFI relativo ao RMS de 16 barras.", "unbounded", "ofi acima da dispersao -> >1"); }
}
