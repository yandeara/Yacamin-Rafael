package br.com.yacamin.rafael.application.service.cache.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.microstructure.BodyCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class BodyCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private final CloseCacheService closeCacheService;

    private String key(String symbol, CandleIntervals interval, String suffix) {
        return keyService.getCacheKeyNew(symbol, interval, Frame.MIC_BODY) + suffix;
    }

    // -------------------------
    // BASE
    // -------------------------
    public BodyIndicator getBody(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=body");
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (BodyIndicator) ex.getIndicator();

        BodyIndicator ind = new BodyIndicator(series);
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public BodyAbsIndicator getBodyAbs(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=body_abs");
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (BodyAbsIndicator) ex.getIndicator();

        BodyAbsIndicator ind = new BodyAbsIndicator(getBody(symbol, interval, series));
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public BodyRatioIndicator getBodyRatio(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=body_ratio");
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (BodyRatioIndicator) ex.getIndicator();

        BodyRatioIndicator ind = new BodyRatioIndicator(getBody(symbol, interval, series));
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public BodyAbsPctIndicator getBodyAbsPct(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=body_abs_pct");
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (BodyAbsPctIndicator) ex.getIndicator();

        BodyAbsPctIndicator ind = new BodyAbsPctIndicator(getBodyAbs(symbol, interval, series));
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public BodyPercIndicator getBodyPerc(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=body_perc");
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (BodyPercIndicator) ex.getIndicator();

        BodyPercIndicator ind = new BodyPercIndicator(getBody(symbol, interval, series));
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public BodyEnergyIndicator getBodyEnergy(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=body_energy");
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (BodyEnergyIndicator) ex.getIndicator();

        BodyEnergyIndicator ind = new BodyEnergyIndicator(getBodyAbs(symbol, interval, series));
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public BodyReturnIndicator getBodyReturn(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=body_return");
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (BodyReturnIndicator) ex.getIndicator();

        var close = closeCacheService.getClosePrice(symbol, interval, series);
        BodyReturnIndicator ind = new BodyReturnIndicator(getBody(symbol, interval, series), close);
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // -------------------------
    // WINDOWED (cross-window) — reusa wrappers genéricos do Wick
    // -------------------------
    public LinRegSlopeIndicator getBodyAbsSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=body_abs_slope|w=" + window);
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (LinRegSlopeIndicator) ex.getIndicator();

        LinRegSlopeIndicator ind = new LinRegSlopeIndicator(getBodyAbs(symbol, interval, series), window);
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public MeanIndicator getBodyAbsMa(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=body_abs_ma|w=" + window);
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (MeanIndicator) ex.getIndicator();

        MeanIndicator ind = new MeanIndicator(getBodyAbs(symbol, interval, series), window);
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public StdDevIndicator getBodyAbsVol(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=body_abs_vol|w=" + window);
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (StdDevIndicator) ex.getIndicator();

        StdDevIndicator ind = new StdDevIndicator(getBodyAbs(symbol, interval, series), window);
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public LinRegSlopeIndicator getBodyRatioSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=body_ratio_slope|w=" + window);
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (LinRegSlopeIndicator) ex.getIndicator();

        LinRegSlopeIndicator ind = new LinRegSlopeIndicator(getBodyRatio(symbol, interval, series), window);
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public StdDevIndicator getBodyRatioVol(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=body_ratio_vol|w=" + window);
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (StdDevIndicator) ex.getIndicator();

        StdDevIndicator ind = new StdDevIndicator(getBodyRatio(symbol, interval, series), window);
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public BodySignPersistenceIndicator getBodySignPersistence(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=body_sign_prst|w=" + window);
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (BodySignPersistenceIndicator) ex.getIndicator();

        BodySignPersistenceIndicator ind = new BodySignPersistenceIndicator(getBody(symbol, interval, series), window);
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public BodyRunLenIndicator getBodyRunLen(String symbol, CandleIntervals interval, BarSeries series, int maxLookback) {
        String k = key(symbol, interval, "|type=body_run_len|max=" + maxLookback);
        var ex = (BodyCacheDto) cacheService.get(k);
        if (ex != null) return (BodyRunLenIndicator) ex.getIndicator();

        BodyRunLenIndicator ind = new BodyRunLenIndicator(getBody(symbol, interval, series), maxLookback);
        cacheService.put(k, BodyCacheDto.builder().indicator(ind).build());
        return ind;
    }
}
