package br.com.yacamin.rafael.application.service.cache.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.SlopeCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.volatility.StdCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

@Service
@RequiredArgsConstructor
public class StdCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private StandardDeviationIndicator get(String symbol,
                                           CandleIntervals interval,
                                           BarSeries series,
                                           Frame label,
                                           int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (StdCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        StandardDeviationIndicator std = create(symbol, interval, series, period);

        cacheService.put(key, StdCacheDto.builder()
                .indicator(std)
                .build());

        return std;
    }

    private LinearRegressionSlopeIndicator getSlope(String symbol,
                                                    CandleIntervals interval,
                                                    BarSeries series,
                                                    Frame label,
                                                    Integer period,
                                                    StandardDeviationIndicator indicator) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (SlopeCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        LinearRegressionSlopeIndicator stdSlope = createSlope(series, indicator, period);

        cacheService.put(key, SlopeCacheDto.builder()
                .indicator(stdSlope)
                .build());

        return stdSlope;
    }

    private LinearRegressionSlopeIndicator createSlope(BarSeries series,
                                                       StandardDeviationIndicator indicator,
                                                       Integer period) {
        return new LinearRegressionSlopeIndicator(series, indicator, period);
    }

    private StandardDeviationIndicator create(String symbol,
                                              CandleIntervals interval,
                                              BarSeries series,
                                              Integer period) {
        var closePrice = closeCacheService.getClosePrice(symbol, interval, series);
        return new StandardDeviationIndicator(closePrice, period);
    }

    // =========================================================================
    // STD BASE
    // =========================================================================
    public StandardDeviationIndicator getStd8(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.STD_8, 8); }
    public StandardDeviationIndicator getStd10(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.STD_10, 10); }
    public StandardDeviationIndicator getStd14(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.STD_14, 14); }
    public StandardDeviationIndicator getStd20(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.STD_20, 20); }
    public StandardDeviationIndicator getStd50(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.STD_50, 50); }

    // =========================================================================
    // V3 / V3.1 — JANELAS "24H" / MULTI-SCALE (48/96/288)
    // =========================================================================
    public StandardDeviationIndicator getStd48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.STD_48, 48);
    }

    // NOVO (Sniper): 96
    public StandardDeviationIndicator getStd96(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.STD_96, 96);
    }

    public StandardDeviationIndicator getStd288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.STD_288, 288);
    }

    // =========================================================================
    // SLOPES (para vlt_std_*_slp) — cache de LinearRegressionSlopeIndicator
    // =========================================================================
    public LinearRegressionSlopeIndicator getStd14Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var std = getStd14(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.STD_14_SLP, 14, std);
    }

    public LinearRegressionSlopeIndicator getStd20Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var std = getStd20(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.STD_20_SLP, 20, std);
    }

    public LinearRegressionSlopeIndicator getStd50Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var std = getStd50(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.STD_50_SLP, 50, std);
    }

    public LinearRegressionSlopeIndicator getStd48Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var std = getStd48(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.STD_48_SLP, 48, std);
    }

    public LinearRegressionSlopeIndicator getStd96Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var std = getStd96(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.STD_96_SLP, 96, std);
    }

    public LinearRegressionSlopeIndicator getStd288Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var std = getStd288(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.STD_288_SLP, 288, std);
    }
}
