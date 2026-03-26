package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.Deque;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

public class VpinCalculator {

    private static final MathContext MC = MathContext.DECIMAL64;

    public static BigDecimal compute(BarSeries s, int buckets) {

        int end = s.getEndIndex();
        if (end < buckets) return BigDecimal.ZERO;

        BigDecimal target = averageVolume(s);
        BigDecimal bucketAccum = BigDecimal.ZERO;

        BigDecimal buyVol  = BigDecimal.ZERO;
        BigDecimal sellVol = BigDecimal.ZERO;

        Deque<BigDecimal> list = new ArrayDeque<>(buckets);

        for (int i = end; i >= 1; i--) {

            Bar b = s.getBar(i);
            BigDecimal vol = b.getVolume().bigDecimalValue();

            BigDecimal direction = b.getClosePrice()
                    .minus(b.getOpenPrice())
                    .bigDecimalValue();

            boolean up = direction.signum() >= 0;

            if (up) buyVol  = buyVol.add(vol, MC);
            else    sellVol = sellVol.add(vol, MC);

            bucketAccum = bucketAccum.add(vol, MC);

            if (bucketAccum.compareTo(target) >= 0) {

                BigDecimal imbalance = buyVol
                        .subtract(sellVol, MC)
                        .abs(MC)
                        .divide(bucketAccum, MC);

                list.add(imbalance);

                if (list.size() > buckets)
                    list.pollFirst();

                bucketAccum = BigDecimal.ZERO;
                buyVol  = BigDecimal.ZERO;
                sellVol = BigDecimal.ZERO;
            }
        }

        if (list.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : list)
            sum = sum.add(v, MC);

        return sum.divide(BigDecimal.valueOf(list.size()), MC);
    }

    private static BigDecimal averageVolume(BarSeries s) {
        int end = s.getEndIndex();
        BigDecimal sum = BigDecimal.ZERO;

        for (int i = 0; i <= end; i++) {
            sum = sum.add(s.getBar(i).getVolume().bigDecimalValue());
        }

        return sum.divide(BigDecimal.valueOf(end + 1), MC);
    }
}
