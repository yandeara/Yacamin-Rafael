package br.com.yacamin.rafael.application.service.cache.indicator;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.*;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class RollCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private String key(String symbol, CandleIntervals interval, String suffix) {
        // precisa existir Frame.MIC_ROLL (ou troca pra algum que você já tenha)
        return keyService.getCacheKeyNew(symbol, interval, Frame.MIC_ROLL) + suffix;
    }

    // =========================================================================
    // COV (raw)
    // =========================================================================
    public RollCovIndicator getRollCov(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=roll_cov|w=" + window);
        var existing = (RollCovCacheDto) cacheService.get(k);
        if (existing != null) return existing.getIndicator();

        var close = closeCacheService.getClosePrice(symbol, interval, series);
        RollCovIndicator ind = new RollCovIndicator(close, window);

        cacheService.put(k, RollCovCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // =========================================================================
    // COV (pct)
    // =========================================================================
    public RollCovPctIndicator getRollCovPct(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=roll_cov_pct|w=" + window);
        var existing = (RollCovPctCacheDto) cacheService.get(k);
        if (existing != null) return existing.getIndicator();

        var close = closeCacheService.getClosePrice(symbol, interval, series);
        RollCovPctIndicator ind = new RollCovPctIndicator(close, window);

        cacheService.put(k, RollCovPctCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // =========================================================================
    // SPREAD (raw) = f(cov)
    // =========================================================================
    public RollSpreadIndicator getRollSpread(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=roll_spread|w=" + window);
        var existing = (RollSpreadCacheDto) cacheService.get(k);
        if (existing != null) return existing.getIndicator();

        var cov = getRollCov(symbol, interval, series, window);
        RollSpreadIndicator ind = new RollSpreadIndicator(cov);

        cacheService.put(k, RollSpreadCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // =========================================================================
    // SPREAD (pct) = f(covPct)
    // =========================================================================
    public RollSpreadPctIndicator getRollSpreadPct(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=roll_spread_pct|w=" + window);
        var existing = (RollSpreadPctCacheDto) cacheService.get(k);
        if (existing != null) return existing.getIndicator();

        var covPct = getRollCovPct(symbol, interval, series, window);
        RollSpreadPctIndicator ind = new RollSpreadPctIndicator(covPct);

        cacheService.put(k, RollSpreadPctCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // =========================================================================
    // ZSCORE(spread)
    // =========================================================================
    public ZscoreIndicator getRollSpreadZscore(String symbol, CandleIntervals interval, BarSeries series, int window, int zWindow) {
        String k = key(symbol, interval, "|type=roll_spread_zsc|w=" + window + "|z=" + zWindow);
        var existing = (ZscoreCacheDto) cacheService.get(k);
        if (existing != null) return existing.getIndicator();

        var spread = getRollSpread(symbol, interval, series, window);
        ZscoreIndicator ind = new ZscoreIndicator(spread, zWindow);

        cacheService.put(k, ZscoreCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // =========================================================================
    // ZSCORE(spreadPct)
    // =========================================================================
    public ZscoreIndicator getRollSpreadPctZscore(String symbol, CandleIntervals interval, BarSeries series, int window, int zWindow) {
        String k = key(symbol, interval, "|type=roll_spread_pct_zsc|w=" + window + "|z=" + zWindow);
        var existing = (ZscoreCacheDto) cacheService.get(k);
        if (existing != null) return existing.getIndicator();

        var spreadPct = getRollSpreadPct(symbol, interval, series, window);
        ZscoreIndicator ind = new ZscoreIndicator(spreadPct, zWindow);

        cacheService.put(k, ZscoreCacheDto.builder().indicator(ind).build());
        return ind;
    }
}