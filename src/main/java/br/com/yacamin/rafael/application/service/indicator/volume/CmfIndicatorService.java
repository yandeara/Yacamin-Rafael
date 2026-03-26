package br.com.yacamin.rafael.application.service.indicator.volume;

import br.com.yacamin.rafael.application.service.cache.indicator.volume.CmfCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volume.ObvCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmfIndicatorService {

    private final CmfCacheService cmfCacheService;

    // =============================================================================================
    // Cálculo genérico da CMF
    // =============================================================================================
    private BigDecimal calculate(BarSeries series, ChaikinMoneyFlowIndicator indicator) {
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
            case CMF_20   -> calculate(series, cmfCacheService.getCmf20(symbol, interval, series));

            default ->
                    throw new IllegalArgumentException("Frame ATR não suportado: " + frame);
        };
    }
}
