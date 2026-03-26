package br.com.yacamin.rafael.application.service.cache.indicator.trend;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.trend.SmaCacheDto;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class SmaCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCache;

    private SMAIndicator get(
            String symbol,
            CandleIntervals interval,
            BarSeries series,
            Frame frame,
            int period
    ) {
        String key = keyService.getCacheKeyNew(symbol, interval, frame);

        var existing = (SmaCacheDto) cacheService.get(key);
        if (existing != null) {
            return existing.getIndicator();
        }

        ClosePriceIndicator close = closeCache.getClosePrice(symbol, interval, series);
        SMAIndicator sma = new SMAIndicator(close, period);

        cacheService.put(key, SmaCacheDto.builder()
                .indicator(sma)
                .build());

        return sma;
    }

    // ==========================================================================================
    // SMA individuais
    // ==========================================================================================
    public SMAIndicator getSma3(String s, CandleIntervals i, BarSeries series)  { return get(s, i, series, Frame.SMA_3, 3); }
    public SMAIndicator getSma5(String s, CandleIntervals i, BarSeries series)  { return get(s, i, series, Frame.SMA_5, 5); }
    public SMAIndicator getSma8(String s, CandleIntervals i, BarSeries series)  { return get(s, i, series, Frame.SMA_8, 8); }
    public SMAIndicator getSma10(String s, CandleIntervals i, BarSeries series) { return get(s, i, series, Frame.SMA_10, 10); }
    public SMAIndicator getSma14(String s, CandleIntervals i, BarSeries series) { return get(s, i, series, Frame.SMA_14, 14); }
    public SMAIndicator getSma20(String s, CandleIntervals i, BarSeries series) { return get(s, i, series, Frame.SMA_20, 20); }
    public SMAIndicator getSma50(String s, CandleIntervals i, BarSeries series) { return get(s, i, series, Frame.SMA_50, 50); }
    public SMAIndicator getSma96(String s, CandleIntervals i, BarSeries series) { return get(s, i, series, Frame.SMA_96, 96); }

    // ==========================================================================================
    // V3 — 24h nativo
    // ==========================================================================================
    public SMAIndicator getSma48(String s, CandleIntervals i, BarSeries series) {
        return get(s, i, series, Frame.SMA_48, 48);
    }

    public SMAIndicator getSma288(String s, CandleIntervals i, BarSeries series) {
        return get(s, i, series, Frame.SMA_288, 288);
    }
}
