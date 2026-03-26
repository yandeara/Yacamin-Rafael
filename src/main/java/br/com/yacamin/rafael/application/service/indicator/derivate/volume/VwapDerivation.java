package br.com.yacamin.rafael.application.service.indicator.derivate.volume;

import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

@Service
public class VwapDerivation {

    /**
     * VWAP clássico: SUM(price * volume) / SUM(volume)
     */
    public double vwap(BarSeries series, int lookback) {

        int end = series.getEndIndex();
        int start = end - lookback + 1;

        double pvSum = 0.0;
        double volSum = 0.0;

        for (int i = start; i <= end; i++) {
            Bar b = series.getBar(i);

            double price = b.getClosePrice().doubleValue();
            double vol   = b.getVolume().doubleValue();

            pvSum  += price * vol;
            volSum += vol;
        }

        return pvSum / volSum;   // se volSum == 0 → explode (Regra de Ouro)
    }

    /**
     * VWAP Distance = (close - vwap) / ATR
     */
    public double vwapDistance(double close, double vwap, double atr) {
        return (close - vwap) / atr;  // se atr == 0 → explode (Regra de Ouro)
    }
}
