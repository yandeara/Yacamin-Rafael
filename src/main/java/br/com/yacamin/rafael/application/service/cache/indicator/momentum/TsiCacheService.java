package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.momentum.TsiCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.TsiIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class TsiCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private TsiIndicator get(String symbol,
                             CandleIntervals interval,
                             BarSeries series,
                             Frame label,
                             int longPeriod,
                             int shortPeriod) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (TsiCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        TsiIndicator tsi = create(symbol, interval, series, longPeriod, shortPeriod);

        cacheService.put(key, TsiCacheDto.builder()
                .indicator(tsi)
                .build());

        return tsi;
    }

    private TsiIndicator create(String symbol,
                                CandleIntervals interval,
                                BarSeries series,
                                int longPeriod,
                                int shortPeriod) {

        ClosePriceIndicator close = closeCacheService.getClosePrice(symbol, interval, series);
        return new TsiIndicator(series, close, longPeriod, shortPeriod);
    }

    public TsiIndicator getTsi25_13(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TSI_25_13, 25, 13);
    }

    // =============================================================================================
    // V3 — 24h nativo (adicione TSI_48_25 e TSI_288_144 no enum Frame)
    // =============================================================================================

    public TsiIndicator getTsi48_25(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TSI_48_25, 48, 25);
    }

    public TsiIndicator getTsi288_144(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TSI_288_144, 288, 144);
    }
}
