package br.com.yacamin.rafael.application.service.indicator.microstructure.calc;

import br.com.yacamin.rafael.application.service.indicator.DescribableCalc;
import br.com.yacamin.rafael.application.service.indicator.FeatureDescription;
import br.com.yacamin.rafael.application.service.indicator.cache.range.extension.RangeSlopeExtension;

import org.springframework.stereotype.Component;

@Component
public class RangeLogRangeSlpW20Calc implements DescribableCalc {

    public static double calculate(RangeSlopeExtension slope, int index) {
        return slope.getValue(index).doubleValue();
    }

    @Override
    public FeatureDescription describe() {
        return new FeatureDescription(
                "mic_log_range_slp_w20",
                "Log Range Slope W20",
                "microstructure",
                "linreg_slope(logRange, 20)",
                "Inclinacao da regressao linear do log-range em 20 barras. " +
                "Tendencia na volatilidade Parkinson; slope positivo indica volatilidade crescente de forma sustentada.",
                "unbounded",
                "logRange subindo de 0.003 a 0.008 em 20 barras -> slope ~ 0.00026"
        );
    }
}
