package br.com.yacamin.rafael.application.service.indicator.derivate.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.volatility.BollingerCacheDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BollingerDerivation {

    // Width = upper - lower
    public double width(BollingerCacheDto bb, int index) {
        double up  = bb.getIndicatorUp().getValue(index).doubleValue();
        double low = bb.getIndicatorLow().getValue(index).doubleValue();
        return Math.abs(up - low);
    }

    // Change rate = width_t / width_{t-1} - 1
    public double widthChange(BollingerCacheDto bb, int index) {
        double curr = width(bb, index);
        double prev = width(bb, index - 1);
        return (curr / prev) - 1.0; // prev==0 -> explode (Regra de Ouro)
    }

    // Z-score of width (rolling)
    public double widthZScore(BollingerCacheDto bb, int index, int window) {
        int start = Math.max(0, index - window + 1);
        int n     = index - start + 1;

        double sum   = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double w = width(bb, i);
            sum   += w;
            sumSq += w * w;
        }

        double mean = sum / n;
        double var  = (sumSq / n) - (mean * mean);
        double std  = Math.sqrt(var);

        double last = width(bb, index);
        return (last - mean) / std; // std==0 -> explode
    }
}
