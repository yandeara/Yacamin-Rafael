package br.com.yacamin.rafael.application.service.cache.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.microstructure.HasbCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volume.SvrCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class HasbrouckCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private final CloseCacheService closeCacheService;
    private final SvrCacheService svrCacheService;

    private String key(String symbol, CandleIntervals interval, String suffix) {
        return keyService.getCacheKeyNew(symbol, interval, Frame.MIC_HASB) + suffix;
    }

    // -------------------------------------------------------------------------
    // LAMBDA (raw)
    // -------------------------------------------------------------------------
    public HasbrouckLambdaIndicator getHasbLambda(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=hasb_lambda|w=" + window);

        var existing = (HasbCacheDto) cacheService.get(k);
        if (existing != null) return (HasbrouckLambdaIndicator) existing.getIndicator();

        var close = closeCacheService.getClosePrice(symbol, interval, series);
        var svr   = svrCacheService.getSvrRaw(symbol, interval, series);

        HasbrouckLambdaIndicator ind = new HasbrouckLambdaIndicator(close, svr, window);

        cacheService.put(k, HasbCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // -------------------------------------------------------------------------
    // ZSCORE(lambda) (zWindow)
    // -------------------------------------------------------------------------
    public ZscoreIndicator getHasbZscore(String symbol, CandleIntervals interval, BarSeries series, int window, int zWindow) {
        String k = key(symbol, interval, "|type=hasb_zsc|w=" + window + "|z=" + zWindow);

        var existing = (HasbCacheDto) cacheService.get(k);
        if (existing != null) return (ZscoreIndicator) existing.getIndicator();

        var lambda = getHasbLambda(symbol, interval, series, window);
        ZscoreIndicator ind = new ZscoreIndicator(lambda, zWindow);

        cacheService.put(k, HasbCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // -------------------------------------------------------------------------
    // MA(lambda)
    // -------------------------------------------------------------------------
    public MeanIndicator getHasbMa(String symbol, CandleIntervals interval, BarSeries series, int window, int maWindow) {
        String k = key(symbol, interval, "|type=hasb_ma|w=" + window + "|ma=" + maWindow);

        var existing = (HasbCacheDto) cacheService.get(k);
        if (existing != null) return (MeanIndicator) existing.getIndicator();

        var lambda = getHasbLambda(symbol, interval, series, window);
        MeanIndicator ind = new MeanIndicator(lambda, maWindow);

        cacheService.put(k, HasbCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // -------------------------------------------------------------------------
    // SLOPE(lambda)
    // -------------------------------------------------------------------------
    public LinRegSlopeIndicator getHasbSlope(String symbol, CandleIntervals interval, BarSeries series, int window, int sWindow) {
        String k = key(symbol, interval, "|type=hasb_slope|w=" + window + "|s=" + sWindow);

        var existing = (HasbCacheDto) cacheService.get(k);
        if (existing != null) return (LinRegSlopeIndicator) existing.getIndicator();

        var lambda = getHasbLambda(symbol, interval, series, window);
        LinRegSlopeIndicator ind = new LinRegSlopeIndicator(lambda, sWindow);

        cacheService.put(k, HasbCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // -------------------------------------------------------------------------
    // VOL(lambda)
    // -------------------------------------------------------------------------
    public StdDevIndicator getHasbVol(String symbol, CandleIntervals interval, BarSeries series, int window, int vWindow) {
        String k = key(symbol, interval, "|type=hasb_vol|w=" + window + "|v=" + vWindow);

        var existing = (HasbCacheDto) cacheService.get(k);
        if (existing != null) return (StdDevIndicator) existing.getIndicator();

        var lambda = getHasbLambda(symbol, interval, series, window);
        StdDevIndicator ind = new StdDevIndicator(lambda, vWindow);

        cacheService.put(k, HasbCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // -------------------------------------------------------------------------
    // PERCENTILE(lambda)
    // -------------------------------------------------------------------------
    public PercentileRankIndicator getHasbPercentile(String symbol, CandleIntervals interval, BarSeries series, int window, int pWindow) {
        String k = key(symbol, interval, "|type=hasb_pctile|w=" + window + "|p=" + pWindow);

        var existing = (HasbCacheDto) cacheService.get(k);
        if (existing != null) return (PercentileRankIndicator) existing.getIndicator();

        var lambda = getHasbLambda(symbol, interval, series, window);
        PercentileRankIndicator ind = new PercentileRankIndicator(lambda, pWindow);

        cacheService.put(k, HasbCacheDto.builder().indicator(ind).build());
        return ind;
    }
}
