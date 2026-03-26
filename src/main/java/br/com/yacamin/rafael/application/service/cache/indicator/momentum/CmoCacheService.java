package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.momentum.CmoCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CMOIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class CmoCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private CMOIndicator get(String symbol,
                             CandleIntervals interval,
                             BarSeries series,
                             Frame label,
                             int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (CmoCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        ClosePriceIndicator close = closeCacheService.getClosePrice(symbol, interval, series);
        CMOIndicator cmo = new CMOIndicator(close, period);

        cacheService.put(key, CmoCacheDto.builder()
                .indicator(cmo)
                .build());

        return cmo;
    }

    public CMOIndicator getCmo14(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CMO_14, 14);
    }

    public CMOIndicator getCmo20(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CMO_20, 20);
    }

    // =============================================================================================
    // V3 — 24h nativo
    // (adicione Frame.CMO_48 e Frame.CMO_288 no enum Frame)
    // =============================================================================================

    public CMOIndicator getCmo48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CMO_48, 48);
    }

    public CMOIndicator getCmo288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CMO_288, 288);
    }
}
