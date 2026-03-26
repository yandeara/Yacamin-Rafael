package br.com.yacamin.rafael.application.service.cache.indicator.trend;

import br.com.yacamin.rafael.application.service.cache.dto.trend.DeviationCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.trend.SlopeAccCacheDto;
import br.com.yacamin.rafael.application.service.cache.dto.SlopeCacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.DeviationIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.domain.Frame;
import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.trend.EmaCacheDto;
import br.com.yacamin.rafael.domain.CandleIntervals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Service
@RequiredArgsConstructor
public class EmaCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final CloseCacheService closeCacheService;

    private EMAIndicator get(String symbol,
                             CandleIntervals interval,
                             BarSeries series,
                             Frame label,
                             Integer period) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (EmaCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        EMAIndicator ema = create(symbol, interval, series, period);

        cacheService.put(key, EmaCacheDto.builder()
                .indicator(ema)
                .build());

        return ema;
    }

    private LinearRegressionSlopeIndicator getSlope(String symbol,
                                                    CandleIntervals interval,
                                                    BarSeries series,
                                                    Frame label,
                                                    Integer period,
                                                    EMAIndicator emaIndicator) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (SlopeCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        LinearRegressionSlopeIndicator emaSlope = createSlope(series, emaIndicator, period);

        cacheService.put(key, SlopeCacheDto.builder()
                .indicator(emaSlope)
                .build());

        return emaSlope;
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

        cacheService.put(key, SlopeAccCacheDto.builder()
                .indicator(slopeAcc)
                .build());

        return slopeAcc;
    }

    private DeviationIndicator getSlopeTds(String symbol,
                                            CandleIntervals interval,
                                            Frame label,
                                            LinearRegressionSlopeIndicator linearRegressionSlopeIndicator,
                                            Integer period) {


        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (DeviationCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing.getIndicator();
        }

        DeviationIndicator slopeTds = createSlopeTds(linearRegressionSlopeIndicator, period);

        cacheService.put(key, DeviationCacheDto.builder()
                .indicator(slopeTds)
                .build());

        return slopeTds;
    }

    private EMAIndicator create(String symbol, CandleIntervals interval, BarSeries series, Integer period) {
        var closePrice = closeCacheService.getClosePrice(symbol, interval, series);
        return new EMAIndicator(closePrice, period);
    }

    private LinearRegressionSlopeIndicator createSlope(BarSeries series, EMAIndicator indicator, Integer period) {
        return new LinearRegressionSlopeIndicator(series, indicator, period);
    }

    private DifferenceIndicator createSlopeAcc(LinearRegressionSlopeIndicator indicator) {
        return new DifferenceIndicator(indicator);
    }

    private DeviationIndicator createSlopeTds(LinearRegressionSlopeIndicator indicator, Integer period) {
        return new DeviationIndicator(indicator, period);
    }

    public EMAIndicator getEma8(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_8, 8);
    }

    public EMAIndicator getEma12(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_12, 12);
    }

    public EMAIndicator getEma20(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_20, 20);
    }

    public EMAIndicator getEma21(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_21, 21);
    }

    public EMAIndicator getEma34(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_34, 34);
    }

    public EMAIndicator getEma50(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_50, 50);
    }

    public EMAIndicator getEma55(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_55, 55);
    }

    public EMAIndicator getEma100(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_100, 100);
    }

    public EMAIndicator getEma144(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_144, 144);
    }

    public EMAIndicator getEma200(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_200, 200);
    }

    public EMAIndicator getEma233(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_233, 233);
    }

    //Ema Slope
    public LinearRegressionSlopeIndicator getEma8Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var ema = getEma8(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.TRD_EMA_8_SLP, 8,  ema);
    }

    public LinearRegressionSlopeIndicator getEma20Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var ema = getEma20(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.TRD_EMA_20_SLP, 20, ema);
    }

    public LinearRegressionSlopeIndicator getEma50Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var ema = getEma50(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.TRD_EMA_50_SLP, 50, ema);
    }

    //Ema Slope Acc [Difference between Slopes]
    public DifferenceIndicator getEma8SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var emaSlope = getEma8Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.EMA_8_SLP_ACC, emaSlope);
    }

    public DifferenceIndicator getEma20SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var emaSlope = getEma20Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.EMA_20_SLP_ACC, emaSlope);
    }

    public DifferenceIndicator getEma50SlpAcc(String symbol, CandleIntervals interval, BarSeries series) {
        var emaSlope = getEma50Slp(symbol, interval, series);
        return getSlopeAcc(symbol, interval, Frame.EMA_50_SLP_ACC, emaSlope);
    }

    //Ema Slope TDS
    public DeviationIndicator getEma8SlpTds(String symbol, CandleIntervals interval, BarSeries series) {
        var emaSlope = getEma8Slp(symbol, interval, series);
        return getSlopeTds(symbol, interval, Frame.EMA_8_SLP_TDS, emaSlope, 8);
    }

    public DeviationIndicator getEma20SlpTds(String symbol, CandleIntervals interval, BarSeries series) {
        var emaSlope = getEma20Slp(symbol, interval, series);
        return getSlopeTds(symbol, interval, Frame.EMA_20_SLP_TDS, emaSlope, 20);
    }

    public DeviationIndicator getEma50SlpTds(String symbol, CandleIntervals interval, BarSeries series) {
        var emaSlope = getEma50Slp(symbol, interval, series);
        return getSlopeTds(symbol, interval, Frame.EMA_50_SLP_TDS, emaSlope, 50);
    }

    // =============================================================================================
// V3 ANCHORS — EMA 16 / EMA 32
// =============================================================================================

    public EMAIndicator getEma16(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_16, 16);
    }

    public EMAIndicator getEma32(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.TRD_EMA_32, 32);
    }

// =============================================================================================
// V3 ANCHORS — EMA SLOPE 16 / 32
// =============================================================================================

    public LinearRegressionSlopeIndicator getEma16Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var ema = getEma16(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.TRD_EMA_16_SLP, 16, ema);
    }

    public LinearRegressionSlopeIndicator getEma32Slp(String symbol, CandleIntervals interval, BarSeries series) {
        var ema = getEma32(symbol, interval, series);
        return getSlope(symbol, interval, series, Frame.TRD_EMA_32_SLP, 32, ema);
    }



}
