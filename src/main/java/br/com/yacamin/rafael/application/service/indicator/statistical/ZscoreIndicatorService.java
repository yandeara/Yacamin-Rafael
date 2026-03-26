package br.com.yacamin.rafael.application.service.indicator.statistical;

import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.trend.SmaCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.StdCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZscoreIndicatorService {

    private final SmaCacheService smaCache;
    private final StdCacheService stdCache;
    private final CloseCacheService closeCache;

    // ==========================================================================================
    // CÁLCULO GENÉRICO DO Z-SCORE = (value - mean) / std
    // ==========================================================================================
    private BigDecimal calculateZ(BigDecimal value,
                                  BigDecimal mean,
                                  double stdVal) {

        if (stdVal == 0.0 || Double.isNaN(stdVal)) {
            return null;
        }

        return value
                .subtract(mean)
                .divide(BigDecimal.valueOf(stdVal), RoundingMode.HALF_UP);
    }

    // ==========================================================================================
    // Dispatcher principal — padrão unificado
    // ==========================================================================================
    public BigDecimal calculate(BarSeries series,
                                SymbolCandle candle,
                                Frame frame) {

        String symbol  = candle.getSymbol();
        var interval   = candle.getInterval();
        int last       = series.getEndIndex();

        return switch (frame) {

            // ==================================================================================
            // ZSCORE CLOSE
            // ==================================================================================
            case CLOSE_3_ZSC -> {
                var close = closeCache.getClosePrice(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                var sma   = smaCache.getSma3(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                double sd = stdCache.getStd8(symbol, interval, series)
                        .getValue(last).doubleValue();

                yield calculateZ(close, sma, sd);
            }

            case CLOSE_5_ZSC -> {
                var close = closeCache.getClosePrice(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                var sma   = smaCache.getSma5(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                double sd = stdCache.getStd8(symbol, interval, series)
                        .getValue(last).doubleValue();

                yield calculateZ(close, sma, sd);
            }

            case CLOSE_8_ZSC -> {
                var close = closeCache.getClosePrice(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                var sma   = smaCache.getSma8(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                double sd = stdCache.getStd8(symbol, interval, series)
                        .getValue(last).doubleValue();

                yield calculateZ(close, sma, sd);
            }

            case CLOSE_14_ZSC -> {
                var close = closeCache.getClosePrice(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                var sma   = smaCache.getSma14(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                double sd = stdCache.getStd14(symbol, interval, series)
                        .getValue(last).doubleValue();

                yield calculateZ(close, sma, sd);
            }

            case CLOSE_20_ZSC -> {
                var close = closeCache.getClosePrice(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                var sma   = smaCache.getSma20(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                double sd = stdCache.getStd20(symbol, interval, series)
                        .getValue(last).doubleValue();

                yield calculateZ(close, sma, sd);
            }

            case CLOSE_50_ZSC -> {
                var close = closeCache.getClosePrice(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                var sma   = smaCache.getSma50(symbol, interval, series)
                        .getValue(last).bigDecimalValue();
                double sd = stdCache.getStd50(symbol, interval, series)
                        .getValue(last).doubleValue();

                yield calculateZ(close, sma, sd);
            }

            // ==================================================================================
            default ->
                    throw new IllegalArgumentException("Frame ZScore não suportado: " + frame);
        };
    }
}

