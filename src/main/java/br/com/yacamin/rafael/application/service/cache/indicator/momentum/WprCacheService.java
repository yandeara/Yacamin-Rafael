package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.momentum.WprCacheDto;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.WilliamsRIndicator;

@Service
@RequiredArgsConstructor
public class WprCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private WprCacheDto get(String symbol,
                            CandleIntervals interval,
                            BarSeries series,
                            Frame frame,
                            int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, frame);
        var existing = (WprCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing;
        }

        WilliamsRIndicator wpr = new WilliamsRIndicator(series, period);

        var dto = WprCacheDto.builder()
                .indicator(wpr)
                .build();

        cacheService.put(key, dto);

        return dto;
    }

    public WprCacheDto getWpr14(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.WPR_14, 14);
    }

    public WprCacheDto getWpr28(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.WPR_28, 28);
    }

    public WprCacheDto getWpr42(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.WPR_42, 42);
    }

    // =============================================================================================
    // V3 — 24h nativo (adicione WPR_48 e WPR_288 no enum Frame)
    // =============================================================================================

    public WprCacheDto getWpr48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.WPR_48, 48);
    }

    public WprCacheDto getWpr288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.WPR_288, 288);
    }
}
