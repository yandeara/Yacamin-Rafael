package br.com.yacamin.rafael.application.service.indicator.volume;

import br.com.yacamin.rafael.application.service.cache.indicator.volume.ObvCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObvIndicatorService {

    private final ObvCacheService obvCache;

    // =============================================================================================
    // Cálculo genérico da OBV
    // =============================================================================================
    private BigDecimal calculate(BarSeries series, OnBalanceVolumeIndicator indicator) {
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
            case OBV   -> calculate(series, obvCache.getObv(symbol, interval, series));

            default ->
                    throw new IllegalArgumentException("Frame ATR não suportado: " + frame);
        };
    }
}
