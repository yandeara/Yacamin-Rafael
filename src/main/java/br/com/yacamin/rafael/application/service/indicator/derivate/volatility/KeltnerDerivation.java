package br.com.yacamin.rafael.application.service.indicator.derivate.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.volatility.BollingerCacheDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.ATRIndicator;

@Service
@RequiredArgsConstructor
public class KeltnerDerivation {

    // width = 4 × ATR (equivale a multiplier=2.0 no canal)
    private static final double KELT_WIDTH_FACTOR = 4.0;

    // Bollinger width = |upper - lower|
    public double bollWidth(BollingerCacheDto bb, int index) {
        double up  = bb.getIndicatorUp().getValue(index).doubleValue();
        double low = bb.getIndicatorLow().getValue(index).doubleValue();
        return Math.abs(up - low);
    }

    // Keltner width = 4 × ATR(period)
    public double keltnerWidth(ATRIndicator atr, int index) {
        double v = atr.getValue(index).doubleValue();
        return v * KELT_WIDTH_FACTOR;
    }

    // Squeeze = BOLL_width / KELT_width
    public double squeeze(BollingerCacheDto bb, ATRIndicator atr, int index) {
        double bw = bollWidth(bb, index);
        double kw = keltnerWidth(atr, index);
        return bw / kw; // kw==0 -> explode (safe)
    }

    // Squeeze change = sqz(t) / sqz(t-1) - 1
    public double squeezeChange(BollingerCacheDto bb, ATRIndicator atr, int index) {
        double curr = squeeze(bb, atr, index);
        double prev = squeeze(bb, atr, index - 1);
        return (curr / prev) - 1.0; // prev==0 -> explode
    }

    // Z-score rolling do squeeze (computado a partir de bb+atr)
    public double squeezeZScore(BollingerCacheDto bb, ATRIndicator atr, int index, int window) {
        int start = Math.max(1, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double s = squeeze(bb, atr, i);
            sum += s;
            sumSq += s * s;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        double sd = Math.sqrt(variance);

        double last = squeeze(bb, atr, index);
        return (last - mean) / sd; // sd==0 -> explode
    }
}
