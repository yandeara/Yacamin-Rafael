package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.momentum.CciCacheDto;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CCIIndicator;

@Service
@RequiredArgsConstructor
public class CciCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private CciCacheDto get(String symbol,
                            CandleIntervals interval,
                            BarSeries series,
                            Frame frame,
                            int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, frame);
        var existing = (CciCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing;
        }

        CCIIndicator cci = new CCIIndicator(series, period);

        var dto = CciCacheDto.builder()
                .indicator(cci)
                .build();

        cacheService.put(key, dto);

        return dto;
    }

    public CciCacheDto getCci14(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CCI_14, 14);
    }

    public CciCacheDto getCci20(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CCI_20, 20);
    }

    // =============================================================================================
    // V3 — 24h nativo (adicione CCI_48 e CCI_288 no enum Frame)
    // =============================================================================================

    public CciCacheDto getCci48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CCI_48, 48);
    }

    public CciCacheDto getCci288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CCI_288, 288);
    }
}
