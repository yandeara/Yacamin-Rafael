package br.com.yacamin.rafael.application.service.indicator.trend;

import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.SmaCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.StdCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloseDerivativesIndicatorService {

    private final CloseCacheService closeCache;
    private final SmaCacheService smaCache;
    private final StdCacheService stdCacheService;

    // =============================================================================================
    // SLOPE
    // =============================================================================================
    private double calculateSlope(BarSeries series, LinearRegressionSlopeIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // SLOPE ACC
    // =============================================================================================
    private double calculateSlopeAcc(BarSeries series, DifferenceIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // ANGLE = atan( (slope/close) * 1000 )
    // =============================================================================================
    private double calculateSlopeAngle(BarSeries series,
                                           LinearRegressionSlopeIndicator slope,
                                           ClosePriceIndicator close) {

        int last = series.getEndIndex();

        double slopeVl = slope.getValue(last).doubleValue();
        double closeVl = close.getValue(last).doubleValue();

        double ratio  = slopeVl / closeVl;

        return Math.atan(ratio * 1000.0);
    }

    // =============================================================================================
    // Z-SCORE (Close)
    // =============================================================================================
    private double calculateCloseZ(ClosePriceIndicator close,
                                   SMAIndicator sma,
                                   StandardDeviationIndicator std) {

        int last = close.getBarSeries().getEndIndex();

        double c   = close.getValue(last).doubleValue();
        double m   = sma.getValue(last).doubleValue();
        double sd  = std.getValue(last).doubleValue();

        if(sd == 0)
            return 0;

        return (c - m) / sd;
    }

    // =============================================================================================
    // TDS = slope / EMA(slope)
    // =============================================================================================
    private double computeTds(LinearRegressionSlopeIndicator slopeInd,
                              int period,
                              int last) {

        double slope = slopeInd.getValue(last).doubleValue();

        EMAIndicator smooth = new EMAIndicator(slopeInd, period);
        double smoothed = smooth.getValue(last).doubleValue();

        return slope / smoothed;
    }


    // =============================================================================================
    // TMI = abs(slope)
    // =============================================================================================
    private double computeTmi(LinearRegressionSlopeIndicator ind, int last) {
        double slope = ind.getValue(last).doubleValue();
        return Math.abs(slope);
    }


    // =============================================================================================
    // TCP - Trend Channel Position
    // =============================================================================================
    private double computeTrendChannelPos(BarSeries series, int window) {

        int last = series.getEndIndex();
        if (last <= 0) {
            throw new IllegalStateException("Série pequena demais para TCP");
        }

        int start = Math.max(0, last - window + 1);

        double minClose = Double.POSITIVE_INFINITY;
        double maxClose = Double.NEGATIVE_INFINITY;

        for (int i = start; i <= last; i++) {
            double c = series.getBar(i).getClosePrice().doubleValue();
            if (c < minClose) minClose = c;
            if (c > maxClose) maxClose = c;
        }

        double closeNow = series.getBar(last).getClosePrice().doubleValue();
        double range = maxClose - minClose;

        if (range == 0.0) {
            throw new IllegalStateException("range=0 em computeTrendChannelPos");
        }

        return (closeNow - minClose) / range;
    }


    // =============================================================================================
    // Dispatcher Principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();

        var closeIndicator = closeCache.getClosePrice(symbol, interval, series);
        int last = series.getEndIndex();

        return switch (frame) {

            // ================================
            // SLOPE
            // ================================
            case CLOSE_3_SLP  -> calculateSlope(series, closeCache.getClose3Slp(symbol, interval, series));
            case CLOSE_5_SLP  -> calculateSlope(series, closeCache.getClose5Slp(symbol, interval, series));
            case CLOSE_8_SLP  -> calculateSlope(series, closeCache.getClose8Slp(symbol, interval, series));
            case CLOSE_14_SLP -> calculateSlope(series, closeCache.getClose14Slp(symbol, interval, series));
            case CLOSE_20_SLP -> calculateSlope(series, closeCache.getClose20Slp(symbol, interval, series));
            case CLOSE_50_SLP -> calculateSlope(series, closeCache.getClose50Slp(symbol, interval, series));

            // ================================
            // SLOPE ACC
            // ================================
            case CLOSE_3_SLP_ACC  -> calculateSlopeAcc(series, closeCache.getClose3SlpAcc(symbol, interval, series));
            case CLOSE_5_SLP_ACC  -> calculateSlopeAcc(series, closeCache.getClose5SlpAcc(symbol, interval, series));
            case CLOSE_8_SLP_ACC  -> calculateSlopeAcc(series, closeCache.getClose8SlpAcc(symbol, interval, series));
            case CLOSE_14_SLP_ACC -> calculateSlopeAcc(series, closeCache.getClose14SlpAcc(symbol, interval, series));
            case CLOSE_20_SLP_ACC -> calculateSlopeAcc(series, closeCache.getClose20SlpAcc(symbol, interval, series));
            case CLOSE_50_SLP_ACC -> calculateSlopeAcc(series, closeCache.getClose50SlpAcc(symbol, interval, series));

            // ================================
            // ANGLE
            // ================================
            case CLOSE_3_SLP_AGL  -> calculateSlopeAngle(series, closeCache.getClose3Slp(symbol, interval, series), closeIndicator);
            case CLOSE_5_SLP_AGL  -> calculateSlopeAngle(series, closeCache.getClose5Slp(symbol, interval, series), closeIndicator);
            case CLOSE_8_SLP_AGL  -> calculateSlopeAngle(series, closeCache.getClose8Slp(symbol, interval, series), closeIndicator);
            case CLOSE_14_SLP_AGL -> calculateSlopeAngle(series, closeCache.getClose14Slp(symbol, interval, series), closeIndicator);
            case CLOSE_20_SLP_AGL -> calculateSlopeAngle(series, closeCache.getClose20Slp(symbol, interval, series), closeIndicator);
            case CLOSE_50_SLP_AGL -> calculateSlopeAngle(series, closeCache.getClose50Slp(symbol, interval, series), closeIndicator);

            // ================================
            // Z-SCORE
            // ================================
            case CLOSE_3_ZSC  -> calculateCloseZ(closeIndicator, smaCache.getSma3(symbol, interval, series),  stdCacheService.getStd8(symbol, interval, series));
            case CLOSE_5_ZSC  -> calculateCloseZ(closeIndicator, smaCache.getSma5(symbol, interval, series),  stdCacheService.getStd8(symbol, interval, series));
            case CLOSE_8_ZSC  -> calculateCloseZ(closeIndicator, smaCache.getSma8(symbol, interval, series),  stdCacheService.getStd8(symbol, interval, series));
            case CLOSE_14_ZSC -> calculateCloseZ(closeIndicator, smaCache.getSma14(symbol, interval, series), stdCacheService.getStd14(symbol, interval, series));
            case CLOSE_20_ZSC -> calculateCloseZ(closeIndicator, smaCache.getSma20(symbol, interval, series), stdCacheService.getStd20(symbol, interval, series));
            case CLOSE_50_ZSC -> calculateCloseZ(closeIndicator, smaCache.getSma50(symbol, interval, series), stdCacheService.getStd50(symbol, interval, series));

            // ================================
            // TDS (Trend Deviation Strength)
            // slope(close_x) / EMA(slope(close_x))
            // ================================
            case CLOSE_3_SLP_TDS -> computeTds(closeCache.getClose3Slp(symbol, interval, series), 3,  last);
            case CLOSE_5_SLP_TDS -> computeTds(closeCache.getClose5Slp(symbol, interval, series), 5,  last);
            case CLOSE_8_SLP_TDS -> computeTds(closeCache.getClose8Slp(symbol, interval, series), 8,  last);
            case CLOSE_14_SLP_TDS -> computeTds(closeCache.getClose14Slp(symbol, interval, series), 14, last);
            case CLOSE_20_SLP_TDS -> computeTds(closeCache.getClose20Slp(symbol, interval, series), 20, last);
            case CLOSE_50_SLP_TDS -> computeTds(closeCache.getClose50Slp(symbol, interval, series), 50, last);

            // ================================
            // TMI (Trend Magnitude Index)
            // abs(slope)
            // ================================
            case CLOSE_3_SLP_TMI -> computeTmi(closeCache.getClose3Slp(symbol, interval, series), last);
            case CLOSE_5_SLP_TMI -> computeTmi(closeCache.getClose5Slp(symbol, interval, series), last);
            case CLOSE_8_SLP_TMI -> computeTmi(closeCache.getClose8Slp(symbol, interval, series), last);
            case CLOSE_14_SLP_TMI -> computeTmi(closeCache.getClose14Slp(symbol, interval, series), last);
            case CLOSE_20_SLP_TMI -> computeTmi(closeCache.getClose20Slp(symbol, interval, series), last);
            case CLOSE_50_SLP_TMI -> computeTmi(closeCache.getClose50Slp(symbol, interval, series), last);

            // ================================
            // TCP (Trend Channel Position)
            // ================================
            case CLOSE_TCP_W50 -> computeTrendChannelPos(series, 50);

            default ->
                    throw new IllegalArgumentException("Frame Close Derivatives não suportado: " + frame);
        };
    }
}
