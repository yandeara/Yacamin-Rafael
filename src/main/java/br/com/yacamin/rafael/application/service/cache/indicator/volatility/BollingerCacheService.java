package br.com.yacamin.rafael.application.service.cache.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.CacheService;
import br.com.yacamin.rafael.application.service.cache.GetCacheKeyService;
import br.com.yacamin.rafael.application.service.cache.dto.volatility.BollingerCacheDto;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.SmaCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.BollingerWidthIndicator;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

@Service
@RequiredArgsConstructor
public class BollingerCacheService {

    private final CacheService cacheService;
    private final GetCacheKeyService keyService;
    private final StdCacheService stdCacheService;
    private final SmaCacheService smaCacheService;

    private BollingerCacheDto get(String symbol,
                                  CandleIntervals interval,
                                  BarSeries series,
                                  Frame label) {

        String key = keyService.getCacheKeyNew(symbol, interval, label);
        var existing = (BollingerCacheDto) cacheService.get(key);

        if (existing != null) {
            return existing;
        }

        BollingerCacheDto bb = create(symbol, interval, series, label);

        cacheService.put(key, bb);

        return bb;
    }

    private BollingerCacheDto create(String symbol, CandleIntervals intervals, BarSeries series, Frame frame) {
        switch (frame) {

            case BB_10 -> {
                StandardDeviationIndicator std = stdCacheService.getStd10(symbol, intervals, series);
                SMAIndicator sma = smaCacheService.getSma10(symbol, intervals, series);
                return build(series, std, sma);
            }

            case BB_20 -> {
                StandardDeviationIndicator std = stdCacheService.getStd20(symbol, intervals, series);
                SMAIndicator sma = smaCacheService.getSma20(symbol, intervals, series);
                return build(series, std, sma);
            }

            case BB_50 -> {
                StandardDeviationIndicator std = stdCacheService.getStd50(symbol, intervals, series);
                SMAIndicator sma = smaCacheService.getSma50(symbol, intervals, series);
                return build(series, std, sma);
            }

            // =========================================================================
            // V3.1 SNIPER — MULTI-SCALE (precisa existir no Frame + SmaCache + StdCache)
            // =========================================================================
            case BB_48 -> {
                StandardDeviationIndicator std = stdCacheService.getStd48(symbol, intervals, series);
                SMAIndicator sma = smaCacheService.getSma48(symbol, intervals, series);
                return build(series, std, sma);
            }

            case BB_96 -> {
                StandardDeviationIndicator std = stdCacheService.getStd96(symbol, intervals, series);
                SMAIndicator sma = smaCacheService.getSma96(symbol, intervals, series);
                return build(series, std, sma);
            }

            case BB_288 -> {
                StandardDeviationIndicator std = stdCacheService.getStd288(symbol, intervals, series);
                SMAIndicator sma = smaCacheService.getSma288(symbol, intervals, series);
                return build(series, std, sma);
            }

            default -> throw new IllegalStateException("Unexpected value: " + frame);
        }
    }

    private BollingerCacheDto build(BarSeries series,
                                    StandardDeviationIndicator std,
                                    SMAIndicator sma) {

        BollingerBandsMiddleIndicator mid = new BollingerBandsMiddleIndicator(sma);
        BollingerBandsUpperIndicator up   = new BollingerBandsUpperIndicator(mid, std);
        BollingerBandsLowerIndicator low  = new BollingerBandsLowerIndicator(mid, std);

        BollingerWidthIndicator width = new BollingerWidthIndicator(series, up, low, mid);

        return BollingerCacheDto.builder()
                .indicatorLow(low)
                .indicatorMid(mid)
                .indicatorUp(up)
                .indicatorWidth(width)
                .build();
    }

    // =========================================================================
    // Base getters (mantidos)
    // =========================================================================
    public BollingerCacheDto getBb10(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.BB_10);
    }

    public BollingerCacheDto getBb20(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.BB_20);
    }

    public BollingerCacheDto getBb50(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.BB_50);
    }

    // =========================================================================
    // V3.1 SNIPER — Multi-scale getters
    // =========================================================================
    public BollingerCacheDto getBb48(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.BB_48);
    }

    public BollingerCacheDto getBb96(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.BB_96);
    }

    public BollingerCacheDto getBb288(String symbol, CandleIntervals interval, BarSeries series) {
        return get(symbol, interval, series, Frame.BB_288);
    }

    // =========================================================================
    // Width helpers (para vlt_boll_*_width_* e seasonality)
    // =========================================================================
    public BollingerWidthIndicator getBollWidth20(String symbol, CandleIntervals interval, BarSeries series) {
        return getBb20(symbol, interval, series).getIndicatorWidth();
    }

    public BollingerWidthIndicator getBollWidth48(String symbol, CandleIntervals interval, BarSeries series) {
        return getBb48(symbol, interval, series).getIndicatorWidth();
    }

    public BollingerWidthIndicator getBollWidth96(String symbol, CandleIntervals interval, BarSeries series) {
        return getBb96(symbol, interval, series).getIndicatorWidth();
    }

    public BollingerWidthIndicator getBollWidth288(String symbol, CandleIntervals interval, BarSeries series) {
        return getBb288(symbol, interval, series).getIndicatorWidth();
    }
}
