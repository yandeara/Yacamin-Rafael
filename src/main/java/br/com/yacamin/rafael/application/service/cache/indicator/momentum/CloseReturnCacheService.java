package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.momentum.CloseReturnCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

@Service
@RequiredArgsConstructor
public class CloseReturnCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private String key(String symbol, CandleIntervals interval, String suffix) {
        return keyService.getCacheKeyNew(symbol, interval, Frame.MOM_CLOSE_RETURN) + suffix;
    }

    // -------------------------------------------------------------------------
    // Base return ret_w
    // -------------------------------------------------------------------------
    public CloseReturnIndicator getCloseReturn(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=close_ret|w=" + window);

        var existing = (CloseReturnCacheDto) cacheService.get(k);
        if (existing != null) return (CloseReturnIndicator) existing.getIndicator();

        var close = closeCacheService.getClosePrice(symbol, interval, series);
        CloseReturnIndicator ind = new CloseReturnIndicator(close, window);

        cacheService.put(k, CloseReturnCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // Ret_1 helper (muito usado pelos derivados)
    public CloseReturnIndicator getCloseReturn1(String symbol, CandleIntervals interval, BarSeries series) {
        return getCloseReturn(symbol, interval, series, 1);
    }

    // -------------------------------------------------------------------------
    // Derived: Burst / Continuation / Decay / Impulse / Chop (todos em cima do ret_1)
    // -------------------------------------------------------------------------
    public BurstStrengthIndicator getBurstStrength(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=burst_strength|w=" + window);

        var existing = (CloseReturnCacheDto) cacheService.get(k);
        if (existing != null) return (BurstStrengthIndicator) existing.getIndicator();

        Indicator<Num> ret1 = getCloseReturn1(symbol, interval, series);
        BurstStrengthIndicator ind = new BurstStrengthIndicator(ret1, window);

        cacheService.put(k, CloseReturnCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public ContinuationRateIndicator getContinuationRate(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=continuation_rate|w=" + window);

        var existing = (CloseReturnCacheDto) cacheService.get(k);
        if (existing != null) return (ContinuationRateIndicator) existing.getIndicator();

        Indicator<Num> ret1 = getCloseReturn1(symbol, interval, series);
        ContinuationRateIndicator ind = new ContinuationRateIndicator(ret1, window);

        cacheService.put(k, CloseReturnCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public DecayRateIndicator getDecayRate(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=decay_rate|w=" + window);

        var existing = (CloseReturnCacheDto) cacheService.get(k);
        if (existing != null) return (DecayRateIndicator) existing.getIndicator();

        Indicator<Num> ret1 = getCloseReturn1(symbol, interval, series);
        DecayRateIndicator ind = new DecayRateIndicator(ret1, window);

        cacheService.put(k, CloseReturnCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public ImpulseIndicator getImpulse(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=impulse|w=" + window);

        var existing = (CloseReturnCacheDto) cacheService.get(k);
        if (existing != null) return (ImpulseIndicator) existing.getIndicator();

        Indicator<Num> ret1 = getCloseReturn1(symbol, interval, series);
        ImpulseIndicator ind = new ImpulseIndicator(ret1, window);

        cacheService.put(k, CloseReturnCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public ChopRatioIndicator getChopRatio(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=chop_ratio|w=" + window);

        var existing = (CloseReturnCacheDto) cacheService.get(k);
        if (existing != null) return (ChopRatioIndicator) existing.getIndicator();

        Indicator<Num> ret1 = getCloseReturn1(symbol, interval, series);
        ChopRatioIndicator ind = new ChopRatioIndicator(ret1, window);

        cacheService.put(k, CloseReturnCacheDto.builder().indicator(ind).build());
        return ind;
    }
}
