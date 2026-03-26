package br.com.yacamin.rafael.application.service.cache.indicator.microstructure;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.microstructure.RangeCacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.*;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@RequiredArgsConstructor
public class RangeCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private String key(String symbol, CandleIntervals interval, String suffix) {
        return keyService.getCacheKeyNew(symbol, interval, Frame.MIC_RANGE) + suffix;
    }

    // =========================================================================
    // BASE
    // =========================================================================
    public RangeIndicator getRange(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=range");
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (RangeIndicator) ex.getIndicator();

        RangeIndicator ind = new RangeIndicator(series);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public TrueRangeIndicator getTrueRange(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=true_range");
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (TrueRangeIndicator) ex.getIndicator();

        TrueRangeIndicator ind = new TrueRangeIndicator(series);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public Hlc3Indicator getHlc3(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=hlc3");
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (Hlc3Indicator) ex.getIndicator();

        Hlc3Indicator ind = new Hlc3Indicator(series);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public LogRangeIndicator getLogRange(String symbol, CandleIntervals interval, BarSeries series) {
        String k = key(symbol, interval, "|type=log_range");
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (LogRangeIndicator) ex.getIndicator();

        LogRangeIndicator ind = new LogRangeIndicator(getRange(symbol, interval, series));
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // =========================================================================
    // WINDOWED — Range
    // =========================================================================
    public MeanIndicator getRangeMa(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=range_ma|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (MeanIndicator) ex.getIndicator();

        MeanIndicator ind = new MeanIndicator(getRange(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public LaggedMeanIndicator getRangeMaLag(String symbol, CandleIntervals interval, BarSeries series, int window, int lag) {
        String k = key(symbol, interval, "|type=range_ma_lag|w=" + window + "|lag=" + lag);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (LaggedMeanIndicator) ex.getIndicator();

        LaggedMeanIndicator ind = new LaggedMeanIndicator(getRange(symbol, interval, series), window, lag);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public StdDevIndicator getRangeVol(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=range_vol|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (StdDevIndicator) ex.getIndicator();

        StdDevIndicator ind = new StdDevIndicator(getRange(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public LinRegSlopeIndicator getRangeSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=range_slope|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (LinRegSlopeIndicator) ex.getIndicator();

        LinRegSlopeIndicator ind = new LinRegSlopeIndicator(getRange(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // =========================================================================
    // WINDOWED — HLC3
    // =========================================================================
    public MeanIndicator getHlc3Ma(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=hlc3_ma|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (MeanIndicator) ex.getIndicator();

        MeanIndicator ind = new MeanIndicator(getHlc3(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public LinRegSlopeIndicator getHlc3Slope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=hlc3_slope|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (LinRegSlopeIndicator) ex.getIndicator();

        LinRegSlopeIndicator ind = new LinRegSlopeIndicator(getHlc3(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public StdDevIndicator getHlc3Vol(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=hlc3_vol|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (StdDevIndicator) ex.getIndicator();

        StdDevIndicator ind = new StdDevIndicator(getHlc3(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    // =========================================================================
    // WINDOWED — LogRange
    // =========================================================================
    public MeanIgnoreZeroIndicator getLogRangeMaIgnoreZero(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=log_range_ma_ign0|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (MeanIgnoreZeroIndicator) ex.getIndicator();

        MeanIgnoreZeroIndicator ind = new MeanIgnoreZeroIndicator(getLogRange(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public LinRegSlopeIndicator getLogRangeSlope(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=log_range_slope|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (LinRegSlopeIndicator) ex.getIndicator();

        LinRegSlopeIndicator ind = new LinRegSlopeIndicator(getLogRange(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public StdDevIndicator getLogRangeVol(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=log_range_vol|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (StdDevIndicator) ex.getIndicator();

        StdDevIndicator ind = new StdDevIndicator(getLogRange(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }

    public PercentileRankIndicator getLogRangePercentile(String symbol, CandleIntervals interval, BarSeries series, int window) {
        String k = key(symbol, interval, "|type=log_range_pctile|w=" + window);
        var ex = (RangeCacheDto) cacheService.get(k);
        if (ex != null) return (PercentileRankIndicator) ex.getIndicator();

        PercentileRankIndicator ind = new PercentileRankIndicator(getLogRange(symbol, interval, series), window);
        cacheService.put(k, RangeCacheDto.builder().indicator(ind).build());
        return ind;
    }
}
