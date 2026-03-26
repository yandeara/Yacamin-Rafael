package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.momentum.RocCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ROCIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class RocCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private RocCacheDto get(String symbol,
                            CandleIntervals interval,
                            BarSeries series,
                            Frame frame,
                            int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, frame);

        var existing = (RocCacheDto) cacheService.get(key);
        if (existing != null) {
            return existing;
        }

        ClosePriceIndicator closeIndicator = closeCacheService.getClosePrice(symbol, interval, series);
        ROCIndicator indicator = new ROCIndicator(closeIndicator, period);

        var dto = RocCacheDto.builder()
                .indicator(indicator)
                .build();

        cacheService.put(key, dto);
        return dto;
    }

    public RocCacheDto getRoc1(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.ROC_1, 1); }
    public RocCacheDto getRoc2(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.ROC_2, 2); }
    public RocCacheDto getRoc3(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.ROC_3, 3); }
    public RocCacheDto getRoc5(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.ROC_5, 5); }

    // =============================================================================================
    // V3 — 24h nativo (adicione ROC_48 e ROC_288 no enum Frame)
    // =============================================================================================
    public RocCacheDto getRoc48(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.ROC_48, 48); }
    public RocCacheDto getRoc288(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.ROC_288, 288); }
}
