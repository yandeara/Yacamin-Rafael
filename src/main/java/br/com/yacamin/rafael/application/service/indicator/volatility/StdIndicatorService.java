package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.StdCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class StdIndicatorService {

    private static final int SCALE = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final int ZSCORE_WINDOW_STD_20 = 200;
    private static final int WINDOW_VV_STD_20      = 20;
    private static final int SLOPE_WINDOW_STD_20   = 20;

    private final StdCacheService stdCache;

    // =============================================================================================
    // Helpers
    // =============================================================================================

    private double computeStd(StandardDeviationIndicator indicator, int index) {
        return indicator.getValue(index).doubleValue();
    }

    private double computeChangeRate(StandardDeviationIndicator indicator, int index) {
        double curr = indicator.getValue(index).doubleValue();
        double prev = indicator.getValue(index - 1).doubleValue();
        return (curr / prev) - 1.0; // se prev == 0 → explode (Regra de Ouro)
    }

    private double computeStdRatio(StandardDeviationIndicator shortStd,
                                   StandardDeviationIndicator longStd,
                                   int index) {

        double s = shortStd.getValue(index).doubleValue();
        double l = longStd.getValue(index).doubleValue();
        return s / l;  // se l == 0 → explode (Regra de Ouro)
    }

    private double computeExpansionFromRatio(double ratio) {
        return Math.max(ratio - 1.0, 0.0);
    }

    private double computeCompressionFromRatio(double ratio) {
        return Math.max(1.0 - ratio, 0.0);
    }

    private double computeStdZScore(StandardDeviationIndicator indicator, int index, int window) {

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

        return (last - mean) / std;  // se std == 0 → explode → correto
    }

    private double computeStdVolOfVol(StandardDeviationIndicator indicator, int index, int window) {

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

        return Math.sqrt(variance);  // se variance < 0 → retorna NaN → ok
    }

    private double computeSlope(BarSeries series, LinearRegressionSlopeIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        return switch (frame) {

            // Nível de STD
            case STD_8  -> computeStd(
                    stdCache.getStd8(symbol, interval, series),
                    last
            );
            case STD_14 -> computeStd(
                    stdCache.getStd14(symbol, interval, series),
                    last
            );
            case STD_20 -> computeStd(
                    stdCache.getStd20(symbol, interval, series),
                    last
            );
            case STD_50 -> computeStd(
                    stdCache.getStd50(symbol, interval, series),
                    last
            );

            // Change rate
            case STD_14_CHG -> computeChangeRate(
                    stdCache.getStd14(symbol, interval, series),
                    last
            );
            case STD_20_CHG -> computeChangeRate(
                    stdCache.getStd20(symbol, interval, series),
                    last
            );
            case STD_50_CHG -> computeChangeRate(
                    stdCache.getStd50(symbol, interval, series),
                    last
            );

            // Regime STD 14/50
            case STD_14_50_RATIO -> {
                var std14 = stdCache.getStd14(symbol, interval, series);
                var std50 = stdCache.getStd50(symbol, interval, series);
                yield computeStdRatio(std14, std50, last);
            }
            case STD_14_50_EXPN -> {
                var std14 = stdCache.getStd14(symbol, interval, series);
                var std50 = stdCache.getStd50(symbol, interval, series);
                double ratio = computeStdRatio(std14, std50, last);
                yield computeExpansionFromRatio(ratio);
            }
            case STD_14_50_CMPR -> {
                var std14 = stdCache.getStd14(symbol, interval, series);
                var std50 = stdCache.getStd50(symbol, interval, series);
                double ratio = computeStdRatio(std14, std50, last);
                yield computeCompressionFromRatio(ratio);
            }

            // STD 20 Z-score
            case STD_20_ZSC -> computeStdZScore(
                    stdCache.getStd20(symbol, interval, series),
                    last,
                    ZSCORE_WINDOW_STD_20
            );

            // STD_20_VV_20: vol-of-vol de STD_20 (std em janela 20)
            case STD_20_VV_W20 -> computeStdVolOfVol(
                    stdCache.getStd20(symbol, interval, series),
                    last,
                    20
            );

            // Slope de STD_20
            case STD_20_SLP -> computeSlope(
                    series,
                    stdCache.getStd20Slp(symbol, interval, series)
            );

            default -> throw new IllegalArgumentException("Frame STD não suportado: " + frame);
        };
    }
}
