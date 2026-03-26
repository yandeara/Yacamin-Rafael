package br.com.yacamin.rafael.application.service.cache.indicator;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.KyleCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.KyleLambdaIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.KyleSignedIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class KyleCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private final CloseCacheService closeCacheService;
    private final OfiCacheService ofiCacheService;

    private String key(String symbol, CandleIntervals interval, String suffix) {
        // precisa existir Frame.MIC_KYLE
        return keyService.getCacheKeyNew(symbol, interval, Frame.MIC_KYLE) + suffix;
    }

    // =========================================================================
    // KYLE LAMBDA (window)
    // =========================================================================
    public KyleLambdaIndicator getKyleLambda(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=kyle_lambda|w=" + window);

        var existing = (KyleCacheDto) cacheService.get(k);
        if (existing != null) return (KyleLambdaIndicator) existing.getIndicator();

        var close = closeCacheService.getClosePrice(symbol, interval, series);
        var ofi   = ofiCacheService.getOfiRaw(symbol, interval, series);

        KyleLambdaIndicator ind = new KyleLambdaIndicator(close, ofi, window);

        cacheService.put(k, KyleCacheDto.builder()
                .indicator(ind)
                .build());

        return ind;
    }

    // =========================================================================
    // KYLE SIGNED (instant)
    // =========================================================================
    public KyleSignedIndicator getKyleSigned(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=kyle_signed");

        var existing = (KyleCacheDto) cacheService.get(k);
        if (existing != null) return (KyleSignedIndicator) existing.getIndicator();

        var close = closeCacheService.getClosePrice(symbol, interval, series);
        var ofi   = ofiCacheService.getOfiRaw(symbol, interval, series);

        KyleSignedIndicator ind = new KyleSignedIndicator(close, ofi);

        cacheService.put(k, KyleCacheDto.builder()
                .indicator(ind)
                .build());

        return ind;
    }
}
