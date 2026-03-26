package br.com.yacamin.rafael.application.service.indicator.trend;

import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.EmaCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.SmaCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.StdCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.DeviationIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.application.service.indicator.trend.dto.TrendAlignmentDto;
import br.com.yacamin.rafael.application.service.indicator.trend.dto.TrendCrossoverDto;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmaIndicatorService {

    private final EmaCacheService emaCache;
    private final CloseCacheService closeCacheService;
    private final SmaCacheService smaCache;
    private final AtrCacheService atrCache;
    private final StdCacheService stdCache;

    // =============================================================================================
    // Cálculo genérico da EMA
    // =============================================================================================
    private double compute(BarSeries series, EMAIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Cálculo genérico da EMA Slope
    // =============================================================================================
    private double calculateSlope(BarSeries series, LinearRegressionSlopeIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Cálculo genérico da EMA Slope Acc
    // =============================================================================================
    private double calculateSlopeAcc(BarSeries series, DifferenceIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).doubleValue();
    }

    private double calculateSlopeAngle(BarSeries series, LinearRegressionSlopeIndicator slope, ClosePriceIndicator close) {
        int last = series.getEndIndex();

        var slopeVl = slope.getValue(last).doubleValue();
        var closeVl = close.getValue(last).doubleValue();

        double ratio  = slopeVl / closeVl;
        double scaled = ratio * 1000.0;

        return Math.atan(scaled); // [-1.57, +1.57]
    }

    private double calculateZ(EMAIndicator ema,
                              SMAIndicator sma,
                              StandardDeviationIndicator std) {

        int last = ema.getBarSeries().getEndIndex();

        double emaValue = ema.getValue(last).doubleValue();
        double smaValue = sma.getValue(last).doubleValue();
        double stdValue = std.getValue(last).doubleValue();

        if(stdValue == 0) {
            return 0;
        }

        return (emaValue - smaValue) / stdValue;
    }

    private double calculateDistance(BarSeries series,
                                         Indicator<Num> start,
                                         Indicator<Num> end) {

        int last = series.getEndIndex();

        double startValue = start.getValue(last).doubleValue();
        double endValue = end.getValue(last).doubleValue();

        return Math.abs(startValue - endValue);
    }


    // =============================================================================================
    // Cálculo genérico da duração EMA_A > EMA_B
    // =============================================================================================
    private double computeDuration(EMAIndicator a, EMAIndicator b, int last) {
        double count = 0.0;

        for (int i = last; i >= 0; i--) {
            double va = a.getValue(i).doubleValue();
            double vb = b.getValue(i).doubleValue();

            if (va > vb) {
                count++;
            } else {
                break;
            }
        }

        return count;
    }


    // =============================================================================================
    // Calculo de Alinhamento [normalized = CTS fast-mid-slow]
    // =============================================================================================
    private TrendAlignmentDto computeAligment(
            Indicator<Num> fast,
            Indicator<Num> mid,
            Indicator<Num> slow,
            double close,
            int last
    ) {
        double fastValue = fast.getValue(last).doubleValue();
        double midValue  = mid.getValue(last).doubleValue();
        double slowValue = slow.getValue(last).doubleValue();

        int score = 0;
        if (fastValue > midValue) score++;
        if (midValue > slowValue) score++;
        if (fastValue > slowValue) score++;

        double normalized = score / 3.0;
        double binary     = midValue > slowValue ? 1.0 : 0.0;

        double delta      = (midValue - slowValue) / close;

        TrendAlignmentDto dto = new TrendAlignmentDto();
        dto.setScore(score);
        dto.setNormalized(normalized);
        dto.setBinary(binary);
        dto.setDelta(delta);

        return dto;
    }

    // =============================================================================================
    // Calculo de Crossover
    // =============================================================================================
    private TrendCrossoverDto computeCrossover(
            EMAIndicator fast,
            EMAIndicator slow,
            double close,
            ATRIndicator atrIndicator,
            StandardDeviationIndicator stdIndicator,
            int last
    ) {
        TrendCrossoverDto dto = new TrendCrossoverDto();

        double fNow  = fast.getValue(last).doubleValue();
        double sNow  = slow.getValue(last).doubleValue();
        double fPrev = fast.getValue(last - 1).doubleValue();
        double sPrev = slow.getValue(last - 1).doubleValue();

        double atr = atrIndicator.getValue(last).doubleValue();
        double std = stdIndicator.getValue(last).doubleValue();

        // CROSSOVER direction (-1, 0, +1)
        int binary = 0;
        if (fPrev < sPrev && fNow > sNow) binary = 1;   // bullish cross
        if (fPrev > sPrev && fNow < sNow) binary = -1;  // bearish cross
        dto.setBinary(binary);

        // DELTA normalizado pelo preço de fechamento
        double delta = (fNow - sNow) / close;
        dto.setDelta(delta);

        // DELTA normalizado pelo ATR e STD
        dto.setAtrN(delta / atr);
        dto.setStdN(delta / std);

        return dto;
    }



    private double computeTds(BarSeries series, DeviationIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Cálculo genérico: EMA_A / EMA_B
    // =============================================================================================
    private double calculateRatio(BarSeries series,
                                 EMAIndicator emaA,
                                 EMAIndicator emaB) {

        int last = series.getEndIndex();

        double a = emaA.getValue(last).doubleValue();
        double b = emaB.getValue(last).doubleValue();

        return a / b;
    }

    // =============================================================================================
    // Cálculo Slope TVR
    // =============================================================================================
    private double calculateTvr(ATRIndicator atrIndicator, LinearRegressionSlopeIndicator linearRegressionSlopeIndicator, int last) {

        double slope = linearRegressionSlopeIndicator.getValue(last).doubleValue();
        double atr = atrIndicator.getValue(last).doubleValue();

        double absSlope = Math.abs(slope);

        return atr / absSlope;
    }

    // ============================================================
    // CTS CLOSE - EMA - Window
    // ============================================================
    private double computeCloseEmaWindow(
            BarSeries series,
            EMAIndicator emaIndicator,
            int window,
            int last
    ) {
        int up = 0;

        for (int i = last - window + 1; i <= last; i++) {
            double close = series.getBar(i).getClosePrice().doubleValue();
            double ema   = emaIndicator.getValue(i).doubleValue();

            if (Double.compare(close, ema) > 0) {
                up++;
            }
        }

        return (double) up / (double) window;
    }

    // ============================================================
    // CTS EMA-8-20-50
    // ============================================================
    private double computeCtsEma(
            EMAIndicator fast,
            EMAIndicator mid,
            EMAIndicator slow,
            int last
    ) {
        double ema8  = fast.getValue(last).doubleValue();
        double ema20 = mid.getValue(last).doubleValue();
        double ema50 = slow.getValue(last).doubleValue();

        int score = 0;

        if (Double.compare(ema8, ema20) > 0) score++;
        if (Double.compare(ema20, ema50) > 0) score++;
        if (Double.compare(ema8, ema50) > 0) score++;

        // retorna valor contínuo em [0, 1]
        return score / 3.0;
    }

    // ============================================================
    // EMA Push - Delta
    // ============================================================
    private double computeDelta(Indicator<Num> a,
                                      Indicator<Num> b,
                                      int last) {

        double av = a.getValue(last).doubleValue();
        double bv = b.getValue(last).doubleValue();

        return (av - bv);
    }

    // ============================================================
    // Slope TCS - Trend Continuation Score (10)
    // ============================================================
    private double computeTcs(Indicator<Num> fast,
                              Indicator<Num> mid,
                              Indicator<Num> slow,
                              int last) {

        double fst = fast.getValue(last).doubleValue();
        double md  = mid.getValue(last).doubleValue();
        double slw = slow.getValue(last).doubleValue();

        return (fst + md + slw) / 3.0;
    }

    // ============================================================
    // Trend Maturity - TM (Windowed)
    // ============================================================
    private double computeTrendMaturity(EMAIndicator start,
                                        EMAIndicator end,
                                        int window,
                                        int last) {

        double duration = computeDuration(start, end, last);

        // limite superior
        double capped = Math.min(duration, (double) window);

        // maturity ∈ [0, 1]
        return capped / (double) window;
    }

    // ============================================================
    // Ema Slope SNR - Signal-to-Noise Ratio
    // ============================================================
    private double computeSlopeSnr(BarSeries series,
                                   LinearRegressionSlopeIndicator linearRegressionSlopeIndicator,
                                   int window,
                                   int last) {

        // slope do indicador, agora como double
        double slope = linearRegressionSlopeIndicator.getValue(last).doubleValue();

        // sinal do slope: -1, 0, +1
        int dir = (slope > 0 ? 1 : (slope < 0 ? -1 : 0));

        if (dir == 0) {
            return 0.0;
        }

        double[] rets = new double[window];
        double sumAligned = 0.0;

        // coleta retornos
        for (int i = 0; i < window; i++) {
            int idx = last - i;
            int idxPrev = idx - 1;

            if (idxPrev < 0) break;

            double closeNow  = series.getBar(idx).getClosePrice().doubleValue();
            double closePrev = series.getBar(idxPrev).getClosePrice().doubleValue();

            double r = (closeNow / closePrev) - 1.0;

            // posição correta (ordem temporal)
            rets[window - 1 - i] = r;

            // retorno alinhado com o sinal
            sumAligned += dir * r;
        }

        int n = window;

        // média dos alinhados
        double meanAligned = sumAligned / n;

        // média normal
        double mean = 0.0;
        for (double r : rets) {
            mean += r;
        }
        mean /= n;

        // variância populacional
        double var = 0.0;
        for (double r : rets) {
            double diff = r - mean;
            var += diff * diff;
        }
        var /= n;

        double std = Math.sqrt(var);

        if (std == 0.0) {
            return 0.0;
        }

        return meanAligned / std;
    }



    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol  = candle.getSymbol();
        var interval   = candle.getInterval();
        int last      = series.getEndIndex();

        return switch (frame) {

            //Indicadores Primarios
            case TRD_EMA_8 -> compute(series, emaCache.getEma8(symbol, interval, series));
            case TRD_EMA_12 -> compute(series, emaCache.getEma12(symbol, interval, series));
            case TRD_EMA_20 -> compute(series, emaCache.getEma20(symbol, interval, series));
            case TRD_EMA_21 -> compute(series, emaCache.getEma21(symbol, interval, series));
            case TRD_EMA_34 -> compute(series, emaCache.getEma34(symbol, interval, series));
            case TRD_EMA_50 -> compute(series, emaCache.getEma50(symbol, interval, series));
            case TRD_EMA_55 -> compute(series, emaCache.getEma55(symbol, interval, series));
            case TRD_EMA_100 -> compute(series, emaCache.getEma100(symbol, interval, series));
            case TRD_EMA_144 -> compute(series, emaCache.getEma144(symbol, interval, series));
            case TRD_EMA_200 -> compute(series, emaCache.getEma200(symbol, interval, series));
            case TRD_EMA_233 -> compute(series, emaCache.getEma233(symbol, interval, series));

            case TRD_EMA_8_SLP -> calculateSlope(series, emaCache.getEma8Slp(symbol, interval, series));
            case TRD_EMA_20_SLP -> calculateSlope(series, emaCache.getEma20Slp(symbol, interval, series));
            case TRD_EMA_50_SLP -> calculateSlope(series, emaCache.getEma50Slp(symbol, interval, series));

            case EMA_8_SLP_ACC -> calculateSlopeAcc(series, emaCache.getEma8SlpAcc(symbol, interval, series));
            case EMA_20_SLP_ACC -> calculateSlopeAcc(series, emaCache.getEma20SlpAcc(symbol, interval, series));
            case EMA_50_SLP_ACC -> calculateSlopeAcc(series, emaCache.getEma50SlpAcc(symbol, interval, series));

            case EMA_8_SLP_AGL -> calculateSlopeAngle(series, emaCache.getEma8Slp(symbol, interval, series), closeCacheService.getClosePrice(symbol, interval, series));
            case EMA_20_SLP_AGL -> calculateSlopeAngle(series, emaCache.getEma20Slp(symbol, interval, series), closeCacheService.getClosePrice(symbol, interval, series));
            case EMA_50_SLP_AGL -> calculateSlopeAngle(series, emaCache.getEma50Slp(symbol, interval, series), closeCacheService.getClosePrice(symbol, interval, series));

            case EMA_8_ZSC -> calculateZ(emaCache.getEma8(symbol, interval, series), smaCache.getSma8(symbol, interval, series), stdCache.getStd8(symbol, interval, series));
            case EMA_20_ZSC -> calculateZ(emaCache.getEma20(symbol, interval, series), smaCache.getSma20(symbol, interval, series), stdCache.getStd20(symbol, interval, series));
            case EMA_50_ZSC -> calculateZ(emaCache.getEma50(symbol, interval, series), smaCache.getSma50(symbol, interval, series), stdCache.getStd50(symbol, interval, series));

            case DIST_CLOSE_EMA_8 -> calculateDistance(series, closeCacheService.getClosePrice(symbol, interval, series), emaCache.getEma8(symbol, interval, series));
            case DIST_CLOSE_EMA_20 -> calculateDistance(series, closeCacheService.getClosePrice(symbol, interval, series), emaCache.getEma20(symbol, interval, series));
            case DIST_CLOSE_EMA_50 -> calculateDistance(series, closeCacheService.getClosePrice(symbol, interval, series), emaCache.getEma50(symbol, interval, series));

            case DIST_EMA_8_20 -> calculateDistance(series, emaCache.getEma8(symbol, interval, series), emaCache.getEma20(symbol, interval, series));
            case DIST_EMA_20_50 -> calculateDistance(series, emaCache.getEma20(symbol, interval, series), emaCache.getEma50(symbol, interval, series));
            case DIST_EMA_8_50 -> calculateDistance(series, emaCache.getEma8(symbol, interval, series), emaCache.getEma50(symbol, interval, series));

            case EMA_8_SLP_TDS -> computeTds(series, emaCache.getEma8SlpTds(symbol, interval, series));
            case EMA_20_SLP_TDS -> computeTds(series, emaCache.getEma20SlpTds(symbol, interval, series));
            case EMA_50_SLP_TDS -> computeTds(series, emaCache.getEma50SlpTds(symbol, interval, series));

            case RATIO_EMA_8_20 -> calculateRatio(series, emaCache.getEma8(symbol, interval, series), emaCache.getEma20(symbol, interval, series));
            case RATIO_EMA_20_50 -> calculateRatio(series, emaCache.getEma20(symbol, interval, series), emaCache.getEma50(symbol, interval, series));
            case RATIO_EMA_8_50 -> calculateRatio(series, emaCache.getEma8(symbol, interval, series), emaCache.getEma50(symbol, interval, series));

            case EMA_8_SLP_TVR -> calculateTvr(atrCache.getAtr14(symbol, interval, series), emaCache.getEma8Slp(symbol, interval, series), last);
            case EMA_20_SLP_TVR -> calculateTvr(atrCache.getAtr14(symbol, interval, series), emaCache.getEma20Slp(symbol, interval, series), last);
            case EMA_50_SLP_TVR -> calculateTvr(atrCache.getAtr14(symbol, interval, series), emaCache.getEma50Slp(symbol, interval, series), last);

            case CTS_CLOSE_EMA_20_W10 -> computeCloseEmaWindow(series, emaCache.getEma20(symbol, interval, series), 10, last);
            case CTS_CLOSE_EMA_20_W50 -> computeCloseEmaWindow(series, emaCache.getEma20(symbol, interval, series), 50, last);
            case CTS_EMA_8_20_50 -> computeCtsEma(emaCache.getEma8(symbol, interval, series), emaCache.getEma20(symbol, interval, series), emaCache.getEma50(symbol, interval, series), last);

            case DELTA_CLOSE_EMA_8 -> computeDelta(closeCacheService.getClosePrice(symbol, interval, series), emaCache.getEma8(symbol, interval, series), last);
            case DELTA_CLOSE_EMA_20 -> computeDelta(closeCacheService.getClosePrice(symbol, interval, series), emaCache.getEma20(symbol, interval, series), last);
            case DELTA_CLOSE_EMA_50 -> computeDelta(closeCacheService.getClosePrice(symbol, interval, series), emaCache.getEma50(symbol, interval, series), last);
            case DELTA_EMA_8_20 -> computeDelta(emaCache.getEma8(symbol, interval, series), emaCache.getEma20(symbol, interval, series), last);
            case DELTA_EMA_20_50 -> computeDelta(emaCache.getEma20(symbol, interval, series), emaCache.getEma50(symbol, interval, series), last);

            case EMA_8_20_50_SLP_TCS -> computeTcs(emaCache.getEma8Slp(symbol, interval, series), emaCache.getEma20Slp(symbol, interval, series), emaCache.getEma50Slp(symbol, interval, series), last);

            case TM_EMA_8_20_W10 -> computeTrendMaturity(emaCache.getEma8(symbol, interval, series), emaCache.getEma20(symbol, interval, series), 10, last);

            case EMA_20_SLP_SNR_W10 -> computeSlopeSnr(series, emaCache.getEma20Slp(symbol, interval, series), 10, last);

            default ->
                    throw new IllegalArgumentException("Frame EMA não suportado: " + frame);
        };
    }

    public TrendAlignmentDto calculateAligment(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol  = candle.getSymbol();
        var interval   = candle.getInterval();

        return switch (frame) {
            case ALIGNMENT_8_20_50 -> {
                int last = series.getEndIndex();
                EMAIndicator e8  = emaCache.getEma8(symbol, interval, series);
                EMAIndicator e20 = emaCache.getEma20(symbol, interval, series);
                EMAIndicator e50 = emaCache.getEma50(symbol, interval, series);
                double close = candle.getClose();

                yield computeAligment(e8, e20, e50, close, last);
            }

            default ->
                    throw new IllegalArgumentException("Frame EMA não suportado: " + frame);
        };
    }

    public TrendCrossoverDto calculateCrossover(BarSeries series,
                                       SymbolCandle candle,
                                       Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        return switch (frame) {

            case CROSSOVER_EMA_8_20 -> {
                EMAIndicator fast = emaCache.getEma8(symbol, interval, series);
                EMAIndicator slow = emaCache.getEma20(symbol, interval, series);

                ATRIndicator atr = atrCache.getAtr14(symbol, interval, series);
                StandardDeviationIndicator std = stdCache.getStd20(symbol, interval, series);

                double close = candle.getClose();

                yield computeCrossover(fast, slow, close, atr, std, last);
            }

            case CROSSOVER_EMA_20_50 -> {
                EMAIndicator fast = emaCache.getEma20(symbol, interval, series);
                EMAIndicator slow = emaCache.getEma50(symbol, interval, series);

                ATRIndicator atr = atrCache.getAtr14(symbol, interval, series);
                StandardDeviationIndicator std = stdCache.getStd50(symbol, interval, series);

                double close = candle.getClose();

                yield computeCrossover(fast, slow, close, atr, std, last);
            }

            default -> throw new IllegalArgumentException("Frame Crossover não suportado: " + frame);
        };
    }

    // =============================================================================================
    // Dispatcher de Duração (contagem de candles EMA_A > EMA_B)
    // =============================================================================================
    public double calculateDuration(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        return switch (frame) {

            case DURATION_EMA_8_20 -> {
                var e8  = emaCache.getEma8(symbol, interval, series);
                var e20 = emaCache.getEma20(symbol, interval, series);
                yield computeDuration(e8, e20, last);
            }

            case DURATION_EMA_20_50 -> {
                var e20 = emaCache.getEma20(symbol, interval, series);
                var e50 = emaCache.getEma50(symbol, interval, series);
                yield computeDuration(e20, e50, last);
            }

            case DURATION_EMA_8_50 -> {
                var e8  = emaCache.getEma8(symbol, interval, series);
                var e50 = emaCache.getEma50(symbol, interval, series);
                yield computeDuration(e8, e50, last);
            }

            default ->
                    throw new IllegalArgumentException("Frame Duration EMA não suportado: " + frame);
        };
    }
}
