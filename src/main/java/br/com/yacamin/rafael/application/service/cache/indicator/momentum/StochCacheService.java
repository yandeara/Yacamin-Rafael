package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.StochCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

@Service
@RequiredArgsConstructor
public class StochCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    // mantido por compat (não usado diretamente aqui)
    private final CloseCacheService closeCacheService;

    private StochCacheDto get(String symbol,
                              CandleIntervals interval,
                              BarSeries series,
                              Frame frame,
                              int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, frame);
        var existing = (StochCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing;
        }

        var k = new StochasticOscillatorKIndicator(series, period);
        var d = new StochasticOscillatorDIndicator(k);

        var dto = StochCacheDto.builder()
                .k(k)
                .d(d)
                .build();

        cacheService.put(key, dto);
        return dto;
    }

    public StochCacheDto getStoch14(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.STOCH_14, 14);
    }

    public StochCacheDto getStoch5(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.STOCH_5, 5);
    }

    // =============================================================================================
    // V3 — 24h nativo
    // (adicione Frame.STOCH_48 e Frame.STOCH_288 no enum Frame)
    // =============================================================================================

    public StochCacheDto getStoch48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.STOCH_48, 48);
    }

    public StochCacheDto getStoch288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.STOCH_288, 288);
    }
}
