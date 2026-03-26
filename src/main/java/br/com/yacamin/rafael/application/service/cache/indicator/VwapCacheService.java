package br.com.yacamin.rafael.application.service.cache.indicator;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.VwapCacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.VwapIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class VwapCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private VwapIndicator get(String symbol,
                              CandleIntervals interval,
                              BarSeries series,
                              Frame label) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);

        var existing = (VwapCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        VwapIndicator indicator = create(series);

        cacheService.put(
                key,
                VwapCacheDto.builder().indicator(indicator)
                .build());

        return indicator;
    }

    private VwapIndicator create(BarSeries series) {
        return new VwapIndicator(series);
    }

    public VwapIndicator getVwap(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.VWAP);
    }

}
