package br.com.yacamin.rafael.application.service.cache.indicator;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.OfiCacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

@Service
@RequiredArgsConstructor
public class OfiCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private String key(String symbol, CandleIntervals interval, String suffix) {
        return keyService.getCacheKeyNew(symbol, interval, Frame.VOL_OFI) + suffix;
    }

    // =========================================================================
    // RAW
    // =========================================================================
    public OfiIndicator getOfiRaw(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=ofi_raw");
        var existing = (OfiCacheDto) cacheService.get(k);
        if (existing != null) return (OfiIndicator) existing.getIndicator();

        OfiIndicator ofi = new OfiIndicator(series);

        cacheService.put(k, OfiCacheDto.builder()
                .indicator(ofi)
                .build());

        return ofi;
    }

    // =========================================================================
    // REL (window)
    // =========================================================================
    public OfiRelIndicator getOfiRel(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=ofi_rel|w=" + window);
        var existing = (OfiCacheDto) cacheService.get(k);
        if (existing != null) return (OfiRelIndicator) existing.getIndicator();

        var ofi = getOfiRaw(symbol, interval, series);
        OfiRelIndicator rel = new OfiRelIndicator(ofi, window);

        cacheService.put(k, OfiCacheDto.builder()
                .indicator(rel)
                .build());

        return rel;
    }

    // =========================================================================
    // ZSCORE (window)
    // =========================================================================
    public OfiZscoreIndicator getOfiZscore(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=ofi_zsc|w=" + window);
        var existing = (OfiCacheDto) cacheService.get(k);
        if (existing != null) return (OfiZscoreIndicator) existing.getIndicator();

        var ofi = getOfiRaw(symbol, interval, series);
        OfiZscoreIndicator z = new OfiZscoreIndicator(ofi, window);

        cacheService.put(k, OfiCacheDto.builder()
                .indicator(z)
                .build());

        return z;
    }

    // =========================================================================
    // VOL-OF-VOL (std window)
    // =========================================================================
    public OfiVolOfVolIndicator getOfiVolOfVol(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=ofi_vov|w=" + window);
        var existing = (OfiCacheDto) cacheService.get(k);
        if (existing != null) return (OfiVolOfVolIndicator) existing.getIndicator();

        var ofi = getOfiRaw(symbol, interval, series);
        OfiVolOfVolIndicator vov = new OfiVolOfVolIndicator(ofi, window);

        cacheService.put(k, OfiCacheDto.builder()
                .indicator(vov)
                .build());

        return vov;
    }

    // =========================================================================
    // SLOPE (linreg window)
    // =========================================================================
    public OfiSlopeIndicator getOfiSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=ofi_slp|w=" + window);
        var existing = (OfiCacheDto) cacheService.get(k);
        if (existing != null) return (OfiSlopeIndicator) existing.getIndicator();

        var ofi = getOfiRaw(symbol, interval, series);
        OfiSlopeIndicator slp = new OfiSlopeIndicator(ofi, window);

        cacheService.put(k, OfiCacheDto.builder()
                .indicator(slp)
                .build());

        return slp;
    }

    // =========================================================================
    // FLIP RATE (sign flips window)
    // =========================================================================
    public OfiFlipRateIndicator getOfiFlipRate(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=ofi_flip|w=" + window);
        var existing = (OfiCacheDto) cacheService.get(k);
        if (existing != null) return (OfiFlipRateIndicator) existing.getIndicator();

        var ofi = getOfiRaw(symbol, interval, series);
        OfiFlipRateIndicator flip = new OfiFlipRateIndicator(ofi, window);

        cacheService.put(k, OfiCacheDto.builder()
                .indicator(flip)
                .build());

        return flip;
    }

    // =========================================================================
    // PERSISTENCE (same sign fraction window)
    // =========================================================================
    public OfiPersistenceIndicator getOfiPersistence(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=ofi_prst|w=" + window);
        var existing = (OfiCacheDto) cacheService.get(k);
        if (existing != null) return (OfiPersistenceIndicator) existing.getIndicator();

        var ofi = getOfiRaw(symbol, interval, series);
        OfiPersistenceIndicator prst = new OfiPersistenceIndicator(ofi, window);

        cacheService.put(k, OfiCacheDto.builder()
                .indicator(prst)
                .build());

        return prst;
    }

    // =========================================================================
    // Conveniência (seus defaults mais comuns)
    // =========================================================================
    public OfiRelIndicator getOfiRel10(String symbol, CandleIntervals interval, BarSeries series) {
        return getOfiRel(symbol, interval, series, 10);
    }

    public OfiZscoreIndicator getOfiZscore20(String symbol, CandleIntervals interval, BarSeries series) {
        return getOfiZscore(symbol, interval, series, 20);
    }

    public OfiVolOfVolIndicator getOfiVov20(String symbol, CandleIntervals interval, BarSeries series) {
        return getOfiVolOfVol(symbol, interval, series, 20);
    }

    public OfiSlopeIndicator getOfiSlope20(String symbol, CandleIntervals interval, BarSeries series) {
        return getOfiSlope(symbol, interval, series, 20);
    }

    public OfiFlipRateIndicator getOfiFlipRate20(String symbol, CandleIntervals interval, BarSeries series) {
        return getOfiFlipRate(symbol, interval, series, 20);
    }

    public OfiPersistenceIndicator getOfiPrst20(String symbol, CandleIntervals interval, BarSeries series) {
        return getOfiPersistence(symbol, interval, series, 20);
    }
}
