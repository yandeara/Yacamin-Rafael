package br.com.yacamin.rafael.application.service.cache.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.microstructure.WickCacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class WickCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private String key(String symbol, CandleIntervals interval, String suffix) {
        return keyService.getCacheKeyNew(symbol, interval, Frame.MIC_WICK) + suffix;
    }

    // -------------------------
    // BASE
    // -------------------------
    public UpperWickIndicator getUpperWick(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=upper_wick");
        var ex = (WickCacheDto) cacheService.get(k);
        if (ex != null) return (UpperWickIndicator) ex.getIndicator();

        UpperWickIndicator ind = new UpperWickIndicator(series);
        cacheService.put(k, WickCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public LowerWickIndicator getLowerWick(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=lower_wick");
        var ex = (WickCacheDto) cacheService.get(k);
        if (ex != null) return (LowerWickIndicator) ex.getIndicator();

        LowerWickIndicator ind = new LowerWickIndicator(series);
        cacheService.put(k, WickCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public WickImbalanceIndicator getWickImbalance(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=wick_imb");
        var ex = (WickCacheDto) cacheService.get(k);
        if (ex != null) return (WickImbalanceIndicator) ex.getIndicator();

        var up = getUpperWick(symbol, interval, series);
        var lo = getLowerWick(symbol, interval, series);

        WickImbalanceIndicator ind = new WickImbalanceIndicator(up, lo);
        cacheService.put(k, WickCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public ClosePosNormIndicator getClosePosNorm(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=close_pos_norm");
        var ex = (WickCacheDto) cacheService.get(k);
        if (ex != null) return (ClosePosNormIndicator) ex.getIndicator();

        ClosePosNormIndicator ind = new ClosePosNormIndicator(series);
        cacheService.put(k, WickCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // -------------------------
    // WINDOWED (cross-window)
    // -------------------------
    public MeanIndicator getUpperWickMa(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=upper_wick_ma|w=" + window);
        var ex = (WickCacheDto) cacheService.get(k);
        if (ex != null) return (MeanIndicator) ex.getIndicator();

        MeanIndicator ind = new MeanIndicator(getUpperWick(symbol, interval, series), window);
        cacheService.put(k, WickCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public MeanIndicator getLowerWickMa(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=lower_wick_ma|w=" + window);
        var ex = (WickCacheDto) cacheService.get(k);
        if (ex != null) return (MeanIndicator) ex.getIndicator();

        MeanIndicator ind = new MeanIndicator(getLowerWick(symbol, interval, series), window);
        cacheService.put(k, WickCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public LinRegSlopeIndicator getWickImbalanceSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=wick_imb_slope|w=" + window);
        var ex = (WickCacheDto) cacheService.get(k);
        if (ex != null) return (LinRegSlopeIndicator) ex.getIndicator();

        LinRegSlopeIndicator ind = new LinRegSlopeIndicator(getWickImbalance(symbol, interval, series), window);
        cacheService.put(k, WickCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public StdDevIndicator getWickImbalanceVol(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=wick_imb_vol|w=" + window);
        var ex = (WickCacheDto) cacheService.get(k);
        if (ex != null) return (StdDevIndicator) ex.getIndicator();

        StdDevIndicator ind = new StdDevIndicator(getWickImbalance(symbol, interval, series), window);
        cacheService.put(k, WickCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public LinRegSlopeIndicator getClosePosSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=close_pos_slope|w=" + window);
        var ex = (WickCacheDto) cacheService.get(k);
        if (ex != null) return (LinRegSlopeIndicator) ex.getIndicator();

        LinRegSlopeIndicator ind = new LinRegSlopeIndicator(getClosePosNorm(symbol, interval, series), window);
        cacheService.put(k, WickCacheDto.builder().indicator(ind).build());
        return ind;
    }
}
