package br.com.yacamin.rafael.application.service.cache.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.SlopeCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.volatility.AtrCacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Service
@RequiredArgsConstructor
public class AtrCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private ATRIndicator get(String symbol,
                             CandleIntervals interval,
                             BarSeries series,
                             Frame label,
                             int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (AtrCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        ATRIndicator atr = create(series, period);

        cacheService.put(key, AtrCacheDto.builder()
                .indicator(atr)
                .build());

        return atr;
    }

    private LinearRegressionSlopeIndicator getSlope(String symbol,
                                                    CandleIntervals interval,
                                                    BarSeries series,
                                                    Frame label,
                                                    Integer period,
                                                    ATRIndicator indicator) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (SlopeCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        LinearRegressionSlopeIndicator rsiSlope = createSlope(series, indicator, period);

        cacheService.put(key, SlopeCacheDto.builder()
                .indicator(rsiSlope)
                .build());

        return rsiSlope;
    }

    private LinearRegressionSlopeIndicator createSlope(BarSeries series,
                                                       ATRIndicator indicator,
                                                       Integer period) {
        return new LinearRegressionSlopeIndicator(series, indicator, period);
    }

    private ATRIndicator create(BarSeries series, Integer period) {
        return new ATRIndicator(series, period);
    }

    public ATRIndicator getAtr7(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.ATR_7, 7);
    }

    public ATRIndicator getAtr14(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.ATR_14, 14);
    }

    public ATRIndicator getAtr21(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.ATR_21, 21);
    }

    //Slope
    public LinearRegressionSlopeIndicator getAtr14Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var atr = getAtr14(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.ATR_14_SLP, 14, atr);
    }

    public ATRIndicator getAtr48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.ATR_48, 48);
    }

    public ATRIndicator getAtr96(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.ATR_96, 96);
    }

    public ATRIndicator getAtr288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.ATR_288, 288);
    }

    // Slopes (LinearRegressionSlopeIndicator em cima do ATR)
    public LinearRegressionSlopeIndicator getAtr7Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var atr = getAtr7(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.ATR_7_SLP, 7, atr);
    }

    public LinearRegressionSlopeIndicator getAtr21Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var atr = getAtr21(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.ATR_21_SLP, 21, atr);
    }

    public LinearRegressionSlopeIndicator getAtr48Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var atr = getAtr48(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.ATR_48_SLP, 48, atr);
    }

    public LinearRegressionSlopeIndicator getAtr96Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var atr = getAtr96(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.ATR_96_SLP, 96, atr);
    }

    public LinearRegressionSlopeIndicator getAtr288Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var atr = getAtr288(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.ATR_288_SLP, 288, atr);
    }

}
