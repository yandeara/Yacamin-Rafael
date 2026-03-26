package br.com.yacamin.rafael.application.service.cache.indicator.volume;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.volatility.AtrCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.volume.ObvCacheDto;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;

@Service
@RequiredArgsConstructor
public class ObvCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private OnBalanceVolumeIndicator get(String symbol,
                                         CandleIntervals interval,
                                         BarSeries series,
                                         Frame label) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (ObvCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        OnBalanceVolumeIndicator obv = create(series);

        cacheService.put(key, ObvCacheDto.builder()
                .indicator(obv)
                .build());

        return obv;
    }

    private OnBalanceVolumeIndicator create(BarSeries series) {
        return new OnBalanceVolumeIndicator(series);
    }

    public OnBalanceVolumeIndicator getObv(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.OBV);
    }

}
