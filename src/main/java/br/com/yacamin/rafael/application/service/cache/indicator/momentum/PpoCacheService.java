package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.momentum.PpoCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class PpoCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private PpoCacheDto create(String symbol,
                               CandleIntervals interval,
                               BarSeries series,
                               int fast,
                               int slow,
                               int signalPeriod) {

        ClosePriceIndicator close = closeCacheService.getClosePrice(symbol, interval, series);

        PPOIndicator ppo = new PPOIndicator(close, fast, slow);
        EMAIndicator signal = new EMAIndicator(ppo, signalPeriod);

        return PpoCacheDto.builder()
                .ppo(ppo)
                .signal(signal)
                .build();
    }

    private PpoCacheDto getOrCreate(String symbol,
                                    CandleIntervals interval,
                                    BarSeries series,
                                    Frame frame,
                                    int fast,
                                    int slow,
                                    int signalPeriod) {

        String key = keyService.getCacheKeyNew(symbol, interval, frame);
        var existing = (PpoCacheDto) cacheService.get(key);

        if (existing != null) return existing;

        PpoCacheDto dto = create(symbol, interval, series, fast, slow, signalPeriod);
        cacheService.put(key, dto);
        return dto;
    }

    // =============================================================================================
    // DEFAULT (12/26/9)
    // =============================================================================================
    public PpoCacheDto getPpoDefault(String symbol,
                                     CandleIntervals interval,
                                     BarSeries series) {

        return getOrCreate(symbol, interval, series, Frame.PPO_12_26, 12, 26, 9);
    }

    // =============================================================================================
    // V3 — 24h NATIVO
    // 30m: 48/104/9
    // 5m : 288/576/9
    // =============================================================================================
    public PpoCacheDto getPpo48_104(String symbol, CandleIntervals interval, BarSeries series) {
        return getOrCreate(symbol, interval, series, Frame.PPO_48_104, 48, 104, 9);
    }

    public PpoCacheDto getPpo288_576(String symbol, CandleIntervals interval, BarSeries series) {
        return getOrCreate(symbol, interval, series, Frame.PPO_288_576, 288, 576, 9);
    }
}
