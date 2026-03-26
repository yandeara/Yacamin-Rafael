package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import java.math.BigDecimal;
import java.math.MathContext;

public class SlopeCalculator {

    private static final MathContext MC = MathContext.DECIMAL64;

    public static BigDecimal slope(BigDecimal[] arr) {

        int n = arr.length;
        BigDecimal sumX  = BigDecimal.ZERO;
        BigDecimal sumY  = BigDecimal.ZERO;
        BigDecimal sumXY = BigDecimal.ZERO;
        BigDecimal sumXX = BigDecimal.ZERO;

        for (int i = 0; i < n; i++) {
            BigDecimal x = BigDecimal.valueOf(i);
            BigDecimal y = arr[i];

            sumX  = sumX.add(x, MC);
            sumY  = sumY.add(y, MC);
            sumXY = sumXY.add(x.multiply(y, MC), MC);
            sumXX = sumXX.add(x.multiply(x, MC), MC);
        }

        BigDecimal nn  = BigDecimal.valueOf(n);
        BigDecimal num = sumXY.multiply(nn, MC).subtract(sumX.multiply(sumY, MC));
        BigDecimal den = sumXX.multiply(nn, MC).subtract(sumX.multiply(sumX, MC));

        if (den.signum() == 0) return BigDecimal.ZERO;

        return num.divide(den, MC);
    }
}
