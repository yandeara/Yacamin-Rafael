package br.com.yacamin.rafael.application.service.cache.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.volatility.TrCacheDto;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;

@Service
@RequiredArgsConstructor
public class TrCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private TRIndicator get(String symbol,
                            CandleIntervals interval,
                            BarSeries series,
                            Frame label) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (TrCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        TRIndicator tr = create(series);

        cacheService.put(key, TrCacheDto.builder()
                .indicator(tr)
                .build());

        return tr;
    }

    private TRIndicator create(BarSeries series) {
        return new TRIndicator(series);
    }

    public TRIndicator getTr(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TR);
    }

}
