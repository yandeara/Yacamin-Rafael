package br.com.yacamin.rafael.application.service.cache.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.volatility.RvCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.RealizedVolIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class RvCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private RealizedVolIndicator get(String symbol,
                                     CandleIntervals interval,
                                     BarSeries series,
                                     Frame label,
                                     int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (RvCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        var close = closeCacheService.getClosePrice(symbol, interval, series);
        RealizedVolIndicator rv = new RealizedVolIndicator(close, period);

        cacheService.put(key, RvCacheDto.builder()
                .indicator(rv)
                .build());

        return rv;
    }

    // base
    public RealizedVolIndicator getRv10(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.RV_10, 10);
    }

    public RealizedVolIndicator getRv30(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.RV_30, 30);
    }

    public RealizedVolIndicator getRv50(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.RV_50, 50);
    }

    // V3.1 Sniper multi-scale
    public RealizedVolIndicator getRv48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.RV_48, 48);
    }

    public RealizedVolIndicator getRv96(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.RV_96, 96);
    }

    public RealizedVolIndicator getRv288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.RV_288, 288);
    }
}
