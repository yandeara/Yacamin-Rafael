package br.com.yacamin.rafael.application.service.indicator.volatility;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.TrCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrIndicatorService {

    private final TrCacheService trCache;

    // =============================================================================================
    // Cálculo genérico da ATR
    // =============================================================================================
    private BigDecimal calculate(BarSeries series, TRIndicator indicator) {
        int last = series.getEndIndex();
        return indicator.getValue(last).bigDecimalValue();
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public BigDecimal calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol  = candle.getSymbol();
        var interval   = candle.getInterval();

        return switch (frame) {

            //Indicadores Primarios
            case TR   -> calculate(series, trCache.getTr(symbol, interval, series));

            default ->
                    throw new IllegalArgumentException("Frame TR não suportado: " + frame);
        };
    }
}
