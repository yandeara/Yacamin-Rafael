package br.com.yacamin.rafael.application.service.indicator.trend;

import br.com.yacamin.rafael.application.service.cache.indicator.trend.SmaCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.averages.SMAIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmaIndicatorService {

    private final SmaCacheService smaCacheService;

    // =============================================================================================
    // Cálculo genérico da SMA
    // =============================================================================================
    private BigDecimal calculate(BarSeries series, SMAIndicator indicator) {
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
            case SMA_20   -> calculate(series, smaCacheService.getSma20(symbol, interval, series));

            default ->
                    throw new IllegalArgumentException("Frame ATR não suportado: " + frame);
        };
    }
}
