package br.com.yacamin.rafael.application.service.cache.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.momentum.RsiCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.SlopeCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
@RequiredArgsConstructor
public class RsiCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private RSIIndicator get(String symbol,
                             CandleIntervals interval,
                             BarSeries series,
                             Frame label,
                             int period) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (RsiCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        RSIIndicator rsi = create(symbol, interval, series, period);

        cacheService.put(key, RsiCacheDto.builder()
                .indicator(rsi)
                .build());

        return rsi;
    }

    private LinearRegressionSlopeIndicator getSlope(String symbol,
                                                    CandleIntervals interval,
                                                    BarSeries series,
                                                    Frame label,
                                                    Integer period,
                                                    RSIIndicator rsiIndicator) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (SlopeCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        LinearRegressionSlopeIndicator rsiSlope = createSlope(series, rsiIndicator, period);

        cacheService.put(key, SlopeCacheDto.builder()
                .indicator(rsiSlope)
                .build());

        return rsiSlope;
    }

    private LinearRegressionSlopeIndicator createSlope(BarSeries series,
                                                       RSIIndicator indicator,
                                                       Integer period) {
        return new LinearRegressionSlopeIndicator(series, indicator, period);
    }

    private RSIIndicator create(String symbol,
                                CandleIntervals interval,
                                BarSeries series,
                                Integer period) {

        ClosePriceIndicator closePrice = closeCacheService.getClosePrice(symbol, interval, series);
        return new RSIIndicator(closePrice, period);
    }

    // =============================================================================================
    // RSI puros
    // =============================================================================================

    public RSIIndicator getRsi2(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.RSI_2, 2); }
    public RSIIndicator getRsi3(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.RSI_3, 3); }
    public RSIIndicator getRsi4(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.RSI_4, 4); }
    public RSIIndicator getRsi5(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.RSI_5, 5); }
    public RSIIndicator getRsi6(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.RSI_6, 6); }
    public RSIIndicator getRsi7(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.RSI_7, 7); }
    public RSIIndicator getRsi8(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.RSI_8, 8); }
    public RSIIndicator getRsi9(String symbol, CandleIntervals interval, BarSeries series)  { return get(symbol, interval, series, Frame.RSI_9, 9); }
    public RSIIndicator getRsi10(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.RSI_10, 10); }
    public RSIIndicator getRsi12(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.RSI_12, 12); }
    public RSIIndicator getRsi14(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.RSI_14, 14); }
    public RSIIndicator getRsi16(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.RSI_16, 16); }
    public RSIIndicator getRsi21(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.RSI_21, 21); }
    public RSIIndicator getRsi24(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.RSI_24, 24); }
    public RSIIndicator getRsi28(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.RSI_28, 28); }
    public RSIIndicator getRsi32(String symbol, CandleIntervals interval, BarSeries series) { return get(symbol, interval, series, Frame.RSI_32, 32); }

    // =============================================================================================
    // V3 — RSI 24h nativo (precisa de Frame.RSI_48 e Frame.RSI_288 no enum Frame)
    // =============================================================================================

    public RSIIndicator getRsi48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.RSI_48, 48);
    }

    public RSIIndicator getRsi288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.RSI_288, 288);
    }

    // =============================================================================================
    // Slopes (já existentes)
    // =============================================================================================

    public LinearRegressionSlopeIndicator getRsi2Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var rsi = getRsi2(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.RSI_2_SLP, 2, rsi);
    }

    public LinearRegressionSlopeIndicator getRsi3Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var rsi = getRsi3(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.RSI_3_SLP, 3, rsi);
    }

    public LinearRegressionSlopeIndicator getRsi5Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var rsi = getRsi5(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.RSI_5_SLP, 5, rsi);
    }

    public LinearRegressionSlopeIndicator getRsi7Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var rsi = getRsi7(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.RSI_7_SLP, 7, rsi);
    }

    public LinearRegressionSlopeIndicator getRsi14Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var rsi = getRsi14(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.RSI_14_SLP, 14, rsi);
    }

    public LinearRegressionSlopeIndicator getRsi28Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var rsi = getRsi28(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.RSI_28_SLP, 28, rsi);
    }

    public LinearRegressionSlopeIndicator getRsi48Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var rsi = getRsi48(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.RSI_48_SLP, 48, rsi);
    }

    public LinearRegressionSlopeIndicator getRsi288Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var rsi = getRsi288(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.RSI_288_SLP, 288, rsi);
    }
}
