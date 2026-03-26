package br.com.yacamin.rafael.application.service.cache.indicator.volume;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.volume.CmfCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.volume.EomCacheDto;
import br.com.yacamin.rafael.application.service.indicator.volume.extension.EomSmoothedIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;

@Service
@RequiredArgsConstructor
public class EomCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private EomSmoothedIndicator get(String symbol,
                                     CandleIntervals interval,
                                     BarSeries series,
                                     Frame label,
                                     int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (EomCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        EomSmoothedIndicator eom = create(series, period);

        cacheService.put(key, EomCacheDto.builder()
                .indicator(eom)
                .build());

        return eom;
    }

    private EomSmoothedIndicator create(BarSeries series, int period) {
        return new EomSmoothedIndicator(series, period);
    }

    public EomSmoothedIndicator getEom14(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.EOM_14, 14);
    }

}
