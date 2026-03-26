package br.com.yacamin.rafael.application.service.cache.indicator;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.AmihudCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.AmihudIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class AmihudCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private AmihudIndicator get(String symbol,
                                CandleIntervals interval,
                                BarSeries series,
                                Frame label) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (AmihudCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        var close = closeCacheService.getClosePrice(symbol, interval, series);
        AmihudIndicator amihud = new AmihudIndicator(close);

        cacheService.put(key, AmihudCacheDto.builder()
                .indicator(amihud)
                .build());

        return amihud;
    }

    public AmihudIndicator getAmihudRaw(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.MIC_AMIHUD_RAW);
    }
}
