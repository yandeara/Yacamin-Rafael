package br.com.yacamin.rafael.application.service.indicator;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.VwapCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleMathOscilatoryService {

    private final AtrCacheService atrCache;
    private final VwapCacheService vwapCache;

    private static final double EPS = 1e-9;

    // ========================================================================
    // HELPERS
    // ========================================================================

    private double getAtr14(BarSeries series, String symbol, CandleIntervals interval, int last) {
        double atr = atrCache.getAtr14(symbol, interval, series).getValue(last).doubleValue();
        if (atr <= 0) {
            throw new IllegalStateException("ATR14 zero ou negativo");
        }
        return atr;
    }

    private double getVwap(BarSeries series, String symbol, CandleIntervals interval, int last) {
        return vwapCache.getVwap(symbol, interval, series).getValue(last).doubleValue();
    }

    private double range(double high, double low) {
        double r = high - low;
        if (r <= 0) {
            throw new IllegalStateException("Range inválido");
        }
        return r;
    }

    // ========================================================================
    // INDICADORES
    // ========================================================================

    private double computeCloseOpenNorm(double open, double close, double r) {
        return (close - open) / r;
    }

    private double computeWickPercUp(double high, double open, double close, double r) {
        double topBody = Math.max(open, close);
        double upperWick = high - topBody;
        if (upperWick < 0) throw new IllegalStateException("Upper wick negativa");
        return upperWick / r;
    }

    private double computeWickPercDown(double low, double open, double close, double r) {
        double bottomBody = Math.min(open, close);
        double lowerWick = bottomBody - low;
        if (lowerWick < 0) throw new IllegalStateException("Lower wick negativa");
        return lowerWick / r;
    }

    private double computeBodyPerc(double open, double close, double r) {
        return Math.abs(close - open) / r;
    }

    private double computeRangeAtrRatio(double r, double atr) {
        return r / atr;
    }

    private double computeCloseHlc3Delta(double high, double low, double close) {
        double hlc3 = (high + low + close) / 3.0;
        return close - hlc3;
    }

    private double computeCloseVwapDelta(double close, double vwap) {
        return close - vwap;
    }

    private double computeCloseTriangleScore(double close, double low, double r) {
        double p = (close - low) / r;
        double inner = Math.abs(2 * p - 1);
        return 1 - inner;
    }

    private double computeCandleBalanceScore(double close, double low, double r) {
        double p = (close - low) / r;
        return 2 * p - 1;
    }

    private double computeCandleEnergyRaw(double open, double close, double r) {
        double body = close - open;
        return Math.signum(body) * r * Math.abs(body);
    }

    private double computeCandleEnergyAtrN(double energyRaw, double atr) {
        return energyRaw / atr;
    }

    private double computeShadowImbalanceScore(double up, double down) {
        return up - down;
    }

    private double computeBodyAtrRatio(double open, double close, double atr) {
        return (close - open) / atr;
    }

    // ========================================================================
    // DISPATCH
    // ========================================================================

    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        int last      = series.getEndIndex();

        double open   = candle.getOpen();
        double high   = candle.getHigh();
        double low    = candle.getLow();
        double close  = candle.getClose();

        double r      = range(high, low);
        double body   = close - open;

        String symbol = candle.getSymbol();
        CandleIntervals interval = candle.getInterval();

        switch (frame) {

            case CLOSE_OPEN_NORM:
                return computeCloseOpenNorm(open, close, r);

            case WICK_PERC_UP:
                return computeWickPercUp(high, open, close, r);

            case WICK_PERC_DOWN:
                return computeWickPercDown(low, open, close, r);

            case BODY_PERC:
                return computeBodyPerc(open, close, r);

            case RANGE_ATR_RATIO: {
                double atr = getAtr14(series, symbol, interval, last);
                return computeRangeAtrRatio(r, atr);
            }

            case CLOSE_HLC3_DELTA:
                return computeCloseHlc3Delta(high, low, close);

            case CLOSE_VWAP_DELTA: {
                double vwap = getVwap(series, symbol, interval, last);
                return computeCloseVwapDelta(close, vwap);
            }

            case CLOSE_TRIANGLE_SCORE:
                return computeCloseTriangleScore(close, low, r);

            case CANDLE_BALANCE_SCORE:
                return computeCandleBalanceScore(close, low, r);

            case CANDLE_ENERGY_RAW:
                return computeCandleEnergyRaw(open, close, r);

            case CANDLE_ENERGY_ATRN: {
                double energy = computeCandleEnergyRaw(open, close, r);
                double atr = getAtr14(series, symbol, interval, last);
                return computeCandleEnergyAtrN(energy, atr);
            }

            case SHADOW_IMBALANCE_SCORE: {
                double up = computeWickPercUp(high, open, close, r);
                double down = computeWickPercDown(low, open, close, r);
                return computeShadowImbalanceScore(up, down);
            }

            case BODY_ATR_RATIO: {
                double atr = getAtr14(series, symbol, interval, last);
                return computeBodyAtrRatio(open, close, atr);
            }

            default:
                throw new IllegalArgumentException("Frame Candle Math Oscilatório não suportado: " + frame);
        }
    }
}
