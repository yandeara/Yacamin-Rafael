package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtrIndicatorService {

    private static final int SCALE = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final int ZSCORE_WINDOW_ATR_14 = 200;
    private static final int WINDOW_VV_ATR_14     = 20;
    private static final int SLOPE_WINDOW_ATR_14  = 20;

    private final AtrCacheService atrCache;

    // =============================================================================================
    // Helpers básicos
    // =============================================================================================

    private double computeAtr(ATRIndicator indicator, int index) {
        return indicator.getValue(index).doubleValue();
    }

    private double computeChangeRate(ATRIndicator indicator, int index) {
        double curr = indicator.getValue(index).doubleValue();
        double prev = indicator.getValue(index - 1).doubleValue();
        return (curr / prev) - 1.0;   // se prev == 0 → explode (Regra de Ouro)
    }

    private double computeTrueRange(BarSeries series, int index) {
        Bar bar = series.getBar(index);

        double high = bar.getHighPrice().doubleValue();
        double low  = bar.getLowPrice().doubleValue();

        if (index == 0) {
            return Math.abs(high - low);
        }

        Bar prev = series.getBar(index - 1);
        double prevClose = prev.getClosePrice().doubleValue();

        double highLow       = Math.abs(high - low);
        double highPrevClose = Math.abs(high - prevClose);
        double lowPrevClose  = Math.abs(low - prevClose);

        return Math.max(highLow, Math.max(highPrevClose, lowPrevClose));
    }

    private int getMinuteOfDay(Bar bar) {
        var z = bar.getBeginTime().atZone(ZoneOffset.UTC);
        return z.getHour() * 60 + z.getMinute();
    }

    // =============================================================================================
    // RANGE_ATR_14_LOC e variação
    // =============================================================================================
    private double computeRangeAtrLocal(BarSeries series, ATRIndicator atrIndicator, int index) {
        double tr  = computeTrueRange(series, index);
        double atr = atrIndicator.getValue(index).doubleValue();
        return tr / atr;   // se atr == 0 → explode (Regra de Ouro)
    }

    private double computeRangeAtrLocalChange(BarSeries series, ATRIndicator atrIndicator, int index) {
        double curr = computeRangeAtrLocal(series, atrIndicator, index);
        double prev = computeRangeAtrLocal(series, atrIndicator, index - 1);
        return (curr / prev) - 1.0;   // se prev == 0 → explode (Regra de Ouro)
    }

    // =============================================================================================
    // Regime ATR 7/21 (ratio, expansão, compressão)
    // =============================================================================================
    private double computeAtrRatio(ATRIndicator atr7, ATRIndicator atr21, int index) {
        double a7  = atr7.getValue(index).doubleValue();
        double a21 = atr21.getValue(index).doubleValue();
        return a7 / a21;   // se a21 == 0 → explode (Regra de Ouro)
    }

    private double computeExpansionFromRatio(double ratio) {
        double diff = ratio - 1.0;
        return Math.max(diff, 0.0);
    }

    private double computeCompressionFromRatio(double ratio) {
        double diff = 1.0 - ratio;
        return Math.max(diff, 0.0);
    }

    // =============================================================================================
    // ATR_14_ZSC
    // =============================================================================================
    private double computeAtrZScore(ATRIndicator indicator, int index, int window) {

        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = indicator.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        double std = Math.sqrt(variance);

        double last = indicator.getValue(index).doubleValue();

        return (last - mean) / std;   // se std == 0 → explode (Regra de Ouro)
    }


    // =============================================================================================
    // ATR_14_VV_20 (vol-of-vol de ATR_14)
    // =============================================================================================
    private double computeAtrVolOfVol(ATRIndicator indicator, int index, int window) {

        int start = Math.max(0, index - window + 1);
        int n = index - start + 1;

        double sum = 0.0;
        double sumSq = 0.0;

        for (int i = start; i <= index; i++) {
            double v = indicator.getValue(i).doubleValue();
            sum += v;
            sumSq += v * v;
        }

        double mean = sum / n;
        double variance = (sumSq / n) - (mean * mean);
        return Math.sqrt(variance);   // se variance=0 → retorna 0.0 → natural
    }

    // =============================================================================================
    // ATR_14_SLP (slope via regressão linear)
    // =============================================================================================
    private double computeSlope(BarSeries series, LinearRegressionSlopeIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Sazonalidade: ATR_14_SEASON e RANGE_SEASON
    // =============================================================================================
    private double computeAtrSeasonality(BarSeries series, ATRIndicator atr14, int index) {

        Bar current = series.getBar(index);
        int minute = getMinuteOfDay(current);

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i <= index; i++) {
            Bar b = series.getBar(i);
            if (getMinuteOfDay(b) == minute) {
                sum += atr14.getValue(i).doubleValue();
                count++;
            }
        }

        double mean = sum / count; // se count==0 → divisão explode (Regra de Ouro)

        double currVal = atr14.getValue(index).doubleValue();

        return currVal / mean;  // se mean==0 → explode (Regra de Ouro)
    }

    private double computeRangeSeasonality(BarSeries series, int index) {

        Bar current = series.getBar(index);
        int minute = getMinuteOfDay(current);

        double currTr = computeTrueRange(series, index);

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i <= index; i++) {
            Bar b = series.getBar(i);
            if (getMinuteOfDay(b) == minute) {
                sum += computeTrueRange(series, i);
                count++;
            }
        }

        double mean = sum / count; // se count==0 → explode (Regra de Ouro)

        return currTr / mean; // se mean==0 → explode
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        return switch (frame) {

            // Nível de ATR
            case ATR_7  -> computeAtr(
                    atrCache.getAtr7(symbol, interval, series),
                    last
            );
            case ATR_14 -> computeAtr(
                    atrCache.getAtr14(symbol, interval, series),
                    last
            );
            case ATR_21 -> computeAtr(
                    atrCache.getAtr21(symbol, interval, series),
                    last
            );

            // Change rate da ATR
            case ATR_7_CHG  -> computeChangeRate(
                    atrCache.getAtr7(symbol, interval, series),
                    last
            );
            case ATR_14_CHG -> computeChangeRate(
                    atrCache.getAtr14(symbol, interval, series),
                    last
            );
            case ATR_21_CHG -> computeChangeRate(
                    atrCache.getAtr21(symbol, interval, series),
                    last
            );

            // Range local vs ATR_14
            case RANGE_ATR_14_LOC -> computeRangeAtrLocal(
                    series,
                    atrCache.getAtr14(symbol, interval, series),
                    last
            );
            case RANGE_ATR_14_LOC_CHG -> computeRangeAtrLocalChange(
                    series,
                    atrCache.getAtr14(symbol, interval, series),
                    last
            );

            // Regime ATR 7/21
            case ATR_7_21_RATIO, ATR_7_21_EXPR -> {
                ATRIndicator atr7  = atrCache.getAtr7(symbol, interval, series);
                ATRIndicator atr21 = atrCache.getAtr21(symbol, interval, series);
                yield computeAtrRatio(atr7, atr21, last);
            }
            case ATR_7_21_EXPN -> {
                ATRIndicator atr7  = atrCache.getAtr7(symbol, interval, series);
                ATRIndicator atr21 = atrCache.getAtr21(symbol, interval, series);
                double ratio   = computeAtrRatio(atr7, atr21, last);
                yield computeExpansionFromRatio(ratio);
            }
            case ATR_7_21_CMPR -> {
                ATRIndicator atr7  = atrCache.getAtr7(symbol, interval, series);
                ATRIndicator atr21 = atrCache.getAtr21(symbol, interval, series);
                double ratio   = computeAtrRatio(atr7, atr21, last);
                yield computeCompressionFromRatio(ratio);
            }

            // ATR 14 Z-score
            case ATR_14_ZSC -> computeAtrZScore(
                    atrCache.getAtr14(symbol, interval, series),
                    last,
                    ZSCORE_WINDOW_ATR_14
            );

            // Slope de ATR_14
            case ATR_14_SLP -> computeSlope(
                    series,
                    atrCache.getAtr14Slp(symbol, interval, series)
            );

            // ATR_14_VV_20: vol-of-vol de ATR_14 (std em janela 20)
            case ATR_14_VV_W20 -> computeAtrVolOfVol(
                    atrCache.getAtr14(symbol, interval, series),
                    last,
                    20
            );

            // Sazonalidade
            case ATR_14_SEASON -> computeAtrSeasonality(
                    series,
                    atrCache.getAtr14(symbol, interval, series),
                    last
            );

            //TODO: Não devia esta aqui, mas vamso revisar no futuro
            case RANGE_SEASON -> computeRangeSeasonality(series, last);

            default -> throw new IllegalArgumentException("Frame ATR não suportado: " + frame);
        };
    }
}
