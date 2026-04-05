package br.com.yacamin.rafael.application.service.indicator.volatility.calc;

import br.com.yacamin.rafael.application.service.indicator.cache.volatility.extension.BollingerWidthExtension;
import org.ta4j.core.indicators.ATRIndicator;

/**
 * Squeeze = boll_abs_width / (4 * ATR).
 * Shared helper para todos os squeeze calcs.
 */
public class SqzHelper {
    private static final double KELT_FACTOR = 4.0;

    public static double sqz(BollingerWidthExtension bbW, ATRIndicator atr, int index) {
        double kW = KELT_FACTOR * atr.getValue(index).doubleValue();
        if (Math.abs(kW) < 1e-12) return 0;
        return bbW.getValue(index).doubleValue() / kW;
    }
}
