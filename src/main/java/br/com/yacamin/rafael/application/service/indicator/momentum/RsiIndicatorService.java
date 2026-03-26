package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.RsiCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class RsiIndicatorService {

    private final RsiCacheService rsiCache;

    // =============================================================================================
    // RSI bruto
    // =============================================================================================
    private double compute(RSIIndicator indicator, int last) {
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // SLOPE (Linear Regression Slope)
    // =============================================================================================
    private double computeSlope(BarSeries series, LinearRegressionSlopeIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // DELTA: RSI_t - RSI_{t-1}
    // =============================================================================================
    private double computeDelta(RSIIndicator rsi, int last) {
        double curr = rsi.getValue(last).doubleValue();
        double prev = rsi.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // ROC: (RSI_t - RSI_{t-period}) / RSI_{t-period}
    // =============================================================================================
    private double computeRoc(RSIIndicator rsi, int last, int period) {
        double prev = rsi.getValue(last - period).doubleValue();
        double curr = rsi.getValue(last).doubleValue();
        return (curr - prev) / prev;   // se prev=0 → explode → Regra de Ouro
    }

    // =============================================================================================
    // Velocity = (RSI_t - RSI_{t-d}) / d
    // =============================================================================================
    private double computeVelocity(RSIIndicator rsi, int last, int dist) {
        double curr = rsi.getValue(last).doubleValue();
        double prev = rsi.getValue(last - dist).doubleValue();
        return (curr - prev) / dist;
    }

    // =============================================================================================
    // Slope Velocity = (slope_t - slope_{t-d}) / d
    // =============================================================================================
    private double computeSlopeVelocity(LinearRegressionSlopeIndicator slp, int last, int dist) {
        double curr = slp.getValue(last).doubleValue();
        double prev = slp.getValue(last - dist).doubleValue();
        return (curr - prev) / dist;
    }

    // =============================================================================================
    // Acceleration (segunda diferença)
    // =============================================================================================
    private double computeAcceleration(RSIIndicator rsi, int last, int dist) {
        double v1 = rsi.getValue(last).doubleValue() - rsi.getValue(last - dist).doubleValue();
        double v2 = rsi.getValue(last - dist).doubleValue() - rsi.getValue(last - 2 * dist).doubleValue();
        return v1 - v2;
    }

    // =============================================================================================
    // Slope Acceleration (segunda diferença da slope)
    // =============================================================================================
    private double computeSlopeAcceleration(LinearRegressionSlopeIndicator slp, int last, int dist) {
        double v1 = slp.getValue(last).doubleValue() - slp.getValue(last - dist).doubleValue();
        double v2 = slp.getValue(last - dist).doubleValue() - slp.getValue(last - (2 * dist)).doubleValue();
        return v1 - v2;
    }

    // =============================================================================================
    // DISTMID = |RSI - 50|
    // =============================================================================================
    private double computeDistMid(RSIIndicator rsi, int last) {
        double x = rsi.getValue(last).doubleValue();
        return Math.abs(x - 50.0);
    }

    // =============================================================================================
    // TAIL_UP = max(RSI - upper, 0)
    // =============================================================================================
    private double computeTailUp(RSIIndicator rsi, int last, double upper) {
        double x = rsi.getValue(last).doubleValue();
        double diff = x - upper;
        return diff > 0 ? diff : 0.0;
    }

    // =============================================================================================
    // TAIL_DN = max(lower - RSI, 0)
    // =============================================================================================
    private double computeTailDown(RSIIndicator rsi, int last, double lower) {
        double x = rsi.getValue(last).doubleValue();
        double diff = lower - x;
        return diff > 0 ? diff : 0.0;
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval = candle.getInterval();
        int last = series.getEndIndex();

        return switch (frame) {

            // RSI puro
            case RSI_2 -> compute(rsiCache.getRsi2(symbol, interval, series), last);
            case RSI_3 -> compute(rsiCache.getRsi3(symbol, interval, series), last);
            case RSI_4 -> compute(rsiCache.getRsi4(symbol, interval, series), last);
            case RSI_5 -> compute(rsiCache.getRsi5(symbol, interval, series), last);
            case RSI_6 -> compute(rsiCache.getRsi6(symbol, interval, series), last);
            case RSI_7 -> compute(rsiCache.getRsi7(symbol, interval, series), last);
            case RSI_8 -> compute(rsiCache.getRsi8(symbol, interval, series), last);
            case RSI_9 -> compute(rsiCache.getRsi9(symbol, interval, series), last);
            case RSI_10 -> compute(rsiCache.getRsi10(symbol, interval, series), last);
            case RSI_12 -> compute(rsiCache.getRsi12(symbol, interval, series), last);
            case RSI_14 -> compute(rsiCache.getRsi14(symbol, interval, series), last);
            case RSI_21 -> compute(rsiCache.getRsi21(symbol, interval, series), last);
            case RSI_24 -> compute(rsiCache.getRsi24(symbol, interval, series), last);
            case RSI_28 -> compute(rsiCache.getRsi28(symbol, interval, series), last);

            // Deltas
            case RSI_2_DLT -> computeDelta(rsiCache.getRsi2(symbol, interval, series), last);
            case RSI_3_DLT -> computeDelta(rsiCache.getRsi3(symbol, interval, series), last);
            case RSI_5_DLT -> computeDelta(rsiCache.getRsi5(symbol, interval, series), last);
            case RSI_7_DLT -> computeDelta(rsiCache.getRsi7(symbol, interval, series), last);
            case RSI_14_DLT -> computeDelta(rsiCache.getRsi14(symbol, interval, series), last);

            // ROC
            case RSI_2_ROC -> computeRoc(rsiCache.getRsi2(symbol, interval, series), last, 2);
            case RSI_3_ROC -> computeRoc(rsiCache.getRsi3(symbol, interval, series), last, 3);
            case RSI_5_ROC -> computeRoc(rsiCache.getRsi5(symbol, interval, series), last, 5);
            case RSI_7_ROC -> computeRoc(rsiCache.getRsi7(symbol, interval, series), last, 7);
            case RSI_14_ROC -> computeRoc(rsiCache.getRsi14(symbol, interval, series), last, 14);

            // Velocity (base)
            case RSI_14_VLT -> computeVelocity(rsiCache.getRsi14(symbol, interval, series), last, 14);

            // Slope Velocity
            case RSI_2_SLP_VLT -> computeSlopeVelocity(rsiCache.getRsi2Slp(symbol, interval, series), last, 2);
            case RSI_3_SLP_VLT -> computeSlopeVelocity(rsiCache.getRsi3Slp(symbol, interval, series), last, 3);
            case RSI_5_SLP_VLT -> computeSlopeVelocity(rsiCache.getRsi5Slp(symbol, interval, series), last, 5);
            case RSI_7_SLP_VLT -> computeSlopeVelocity(rsiCache.getRsi7Slp(symbol, interval, series), last, 7);
            case RSI_14_SLP_VLT -> computeSlopeVelocity(rsiCache.getRsi14Slp(symbol, interval, series), last, 14);

            // Aceleração de slope
            case RSI_2_SLP_ACC -> computeSlopeAcceleration(rsiCache.getRsi2Slp(symbol, interval, series), last, 2);
            case RSI_3_SLP_ACC -> computeSlopeAcceleration(rsiCache.getRsi3Slp(symbol, interval, series), last, 3);
            case RSI_5_SLP_ACC -> computeSlopeAcceleration(rsiCache.getRsi5Slp(symbol, interval, series), last, 5);
            case RSI_7_SLP_ACC -> computeSlopeAcceleration(rsiCache.getRsi7Slp(symbol, interval, series), last, 7);
            case RSI_14_SLP_ACC -> computeSlopeAcceleration(rsiCache.getRsi14Slp(symbol, interval, series), last, 14);

            // Aceleração pura
            case RSI_14_ACC -> computeAcceleration(rsiCache.getRsi14(symbol, interval, series), last, 14);

            // Slope bruto (regressão linear)
            case RSI_14_SLP -> computeSlope(series, rsiCache.getRsi14Slp(symbol, interval, series));

            // Distância até o meio
            case RSI_7_DST_MID -> computeDistMid(rsiCache.getRsi7(symbol, interval, series), last);
            case RSI_14_DST_MID -> computeDistMid(rsiCache.getRsi14(symbol, interval, series), last);

            // Caudas superior e inferior
            case RSI_7_TAIL_UP -> computeTailUp(rsiCache.getRsi7(symbol, interval, series), last, 70.0);
            case RSI_7_TAIL_DW -> computeTailDown(rsiCache.getRsi7(symbol, interval, series), last, 30.0);
            case RSI_14_TAIL_UP -> computeTailUp(rsiCache.getRsi14(symbol, interval, series), last, 70.0);
            case RSI_14_TAIL_DW -> computeTailDown(rsiCache.getRsi14(symbol, interval, series), last, 30.0);

            default -> throw new IllegalArgumentException("Frame RSI não suportado: " + frame);
        };
    }
}
