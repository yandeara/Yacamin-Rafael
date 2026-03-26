package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.momentum.TrixCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.TrixIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class TrixCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private TrixIndicator get(String symbol,
                              CandleIntervals interval,
                              BarSeries series,
                              Frame label,
                              int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (TrixCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        TrixIndicator trix = create(symbol, interval, series, period);

        cacheService.put(key, TrixCacheDto.builder()
                .indicator(trix)
                .build());

        return trix;
    }

    private TrixIndicator create(String symbol,
                                 CandleIntervals interval,
                                 BarSeries series,
                                 int period) {

        ClosePriceIndicator close = closeCacheService.getClosePrice(symbol, interval, series);
        return new TrixIndicator(series, close, period);
    }

    public TrixIndicator getTrix9(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRIX_9, 9);
    }

    // =============================================================================================
    // V3 — 24h nativo (adicione TRIX_48 e TRIX_288 no enum Frame)
    // =============================================================================================
    public TrixIndicator getTrix48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRIX_48, 48);
    }

    public TrixIndicator getTrix288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRIX_288, 288);
    }
}
