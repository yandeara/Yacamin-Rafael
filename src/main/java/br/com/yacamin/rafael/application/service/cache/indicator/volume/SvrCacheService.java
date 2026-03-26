package br.com.yacamin.rafael.application.service.cache.indicator.volume;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.volume.SvrCacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.SvrIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class SvrCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private String key(String symbol, CandleIntervals interval, String suffix) {
        return keyService.getCacheKeyNew(symbol, interval, Frame.VOL_SVR) + suffix;
    }

    public SvrIndicator getSvrRaw(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=svr_raw");

        var existing = (SvrCacheDto) cacheService.get(k);
        if (existing != null) return (SvrIndicator) existing.getIndicator();

        SvrIndicator ind = new SvrIndicator(series);

        cacheService.put(k, SvrCacheDto.builder()
                .indicator(ind)
                .build());

        return ind;
    }
}
