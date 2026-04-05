package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeSmaExtension;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeStdExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeCompressionW20Calc implements DescribableCalc {

    private static final double EPS = 1e-12;

    public static double calculate(RangeStdExtension std20, RangeSmaExtension sma20, int index) {
        double smaVal = sma20.getValue(index).doubleValue();
        if (Math.abs(smaVal) < EPS) return 0;
        return std20.getValue(index).doubleValue() / smaVal;
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_range_compression_w20",
                "Range Compression W20",
                "microstructure",
                "std(range, 20) / SMA(range, 20)",
                "Coeficiente de variacao do range em 20 barras. " +
                "Valores baixos indicam compressao uniforme de volatilidade (potencial breakout); valores altos indicam volatilidade erratica.",
                "0+",
                "std20=0.5, sma20=2.0 -> 0.5/2.0 = 0.25"
        );
    }
}
