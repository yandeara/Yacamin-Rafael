package br.com.yacamin.rafael.application.service.cache.indicator.trend;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.trend.AdxCacheDto;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;

@Service
@RequiredArgsConstructor
public class AdxCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    public AdxCacheDto getAdx14(String symbol, CandleIntervals interval, BarSeries series) {

        String key = keyService.getCacheKeyNew(symbol, interval, Frame.TRD_ADX_14);
        var existing = (AdxCacheDto) cacheService.get(key);
        if (existing != null) return existing;

        var adx = new ADXIndicator(series, 14);
        var pdi = new PlusDIIndicator(series, 14);
        var mdi = new MinusDIIndicator(series, 14);

        var dto = AdxCacheDto.builder()
                .adx(adx)
                .pdi(pdi)
                .mdi(mdi)
                .build();

        cacheService.put(key, dto);
        return dto;
    }
}

