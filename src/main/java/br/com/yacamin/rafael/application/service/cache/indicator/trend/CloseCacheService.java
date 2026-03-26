package br.com.yacamin.rafael.application.service.cache.indicator.trend;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.trend.ClosePriceCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.trend.SlopeAccCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.SlopeCacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class CloseCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;

    private ClosePriceIndicator get(String symbol,
                                    CandleIntervals interval,
                                    BarSeries series,
                                    Frame label) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (ClosePriceCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        ClosePriceIndicator indicator = create(series);

        cacheService.put(
                key,
                ClosePriceCacheDto.builder()
                        .indicator(indicator)
                        .build()
        );

        return indicator;
    }

    private LinearRegressionSlopeIndicator getSlope(String symbol,
                                                    CandleIntervals interval,
                                                    BarSeries series,
                                                    Frame label,
                                                    Integer period,
                                                    ClosePriceIndicator closePriceIndicator) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (SlopeCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        LinearRegressionSlopeIndicator slope = createSlope(series, closePriceIndicator, period);

        cacheService.put(
                key,
                SlopeCacheDto.builder()
                        .indicator(slope)
                        .build()
        );

        return slope;
    }

    private DifferenceIndicator getSlopeAcc(String symbol,
                                            CandleIntervals interval,
                                            Frame label,
                                            LinearRegressionSlopeIndicator linearRegressionSlopeIndicator) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (SlopeAccCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        DifferenceIndicator slopeAcc = createSlopeAcc(linearRegressionSlopeIndicator);

        cacheService.put(
                key,
                SlopeAccCacheDto.builder()
                        .indicator(slopeAcc)
                        .build()
        );

        return slopeAcc;
    }

    private ClosePriceIndicator create(BarSeries series) {
        return new ClosePriceIndicator(series);
    }

    private LinearRegressionSlopeIndicator createSlope(BarSeries series,
                                                       ClosePriceIndicator indicator,
                                                       Integer period) {
        return new LinearRegressionSlopeIndicator(series, indicator, period);
    }

    private DifferenceIndicator createSlopeAcc(LinearRegressionSlopeIndicator indicator) {
        return new DifferenceIndicator(indicator);
    }

    // Close simples
    public ClosePriceIndicator getClosePrice(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.CLOSE_PRICE);
    }

    // ==========================================================================================
    // Close Slope
    // ==========================================================================================
    public LinearRegressionSlopeIndicator getClose3Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var close = getClosePrice(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.CLOSE_3_SLP, 3, close);
    }

    public LinearRegressionSlopeIndicator getClose5Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var close = getClosePrice(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.CLOSE_5_SLP, 5, close);
    }

    public LinearRegressionSlopeIndicator getClose8Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var close = getClosePrice(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.CLOSE_8_SLP, 8, close);
    }

    public LinearRegressionSlopeIndicator getClose14Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var close = getClosePrice(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.CLOSE_14_SLP, 14, close);
    }

    public LinearRegressionSlopeIndicator getClose20Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var close = getClosePrice(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.CLOSE_20_SLP, 20, close);
    }

    public LinearRegressionSlopeIndicator getClose50Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var close = getClosePrice(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.CLOSE_50_SLP, 50, close);
    }

    // -------------------------------
    // V3 — 24h nativo
    // -------------------------------
    public LinearRegressionSlopeIndicator getClose48Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var close = getClosePrice(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.CLOSE_48_SLP, 48, close);
    }

    public LinearRegressionSlopeIndicator getClose288Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var close = getClosePrice(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.CLOSE_288_SLP, 288, close);
    }

    // ==========================================================================================
    // Close Slope Acc
    // ==========================================================================================
    public DifferenceIndicator getClose3SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var slope = getClose3Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.CLOSE_3_SLP_ACC, slope);
    }

    public DifferenceIndicator getClose5SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var slope = getClose5Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.CLOSE_5_SLP_ACC, slope);
    }

    public DifferenceIndicator getClose8SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var slope = getClose8Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.CLOSE_8_SLP_ACC, slope);
    }

    public DifferenceIndicator getClose14SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var slope = getClose14Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.CLOSE_14_SLP_ACC, slope);
    }

    public DifferenceIndicator getClose20SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var slope = getClose20Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.CLOSE_20_SLP_ACC, slope);
    }

    public DifferenceIndicator getClose50SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var slope = getClose50Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.CLOSE_50_SLP_ACC, slope);
    }

    // -------------------------------
    // V3 — 24h nativo
    // -------------------------------
    public DifferenceIndicator getClose48SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var slope = getClose48Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.CLOSE_48_SLP_ACC, slope);
    }

    public DifferenceIndicator getClose288SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var slope = getClose288Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.CLOSE_288_SLP_ACC, slope);
    }
}
