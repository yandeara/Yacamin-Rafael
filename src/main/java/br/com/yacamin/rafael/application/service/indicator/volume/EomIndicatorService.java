package br.com.yacamin.rafael.application.service.indicator.volume;

import br.com.yacamin.rafael.application.service.cache.indicator.volume.CmfCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volume.EomCacheService;
import br.com.yacamin.rafael.application.service.indicator.volume.extension.EomSmoothedIndicator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class EomIndicatorService {

    private final CmfCacheService cmfCacheService;
    private final EomCacheService eomCacheService;

    // =============================================================================================
    // Cálculo genérico da EOM
    // =============================================================================================
    private BigDecimal calculate(BarSeries series, EomSmoothedIndicator indicator) {
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
            case EOM_14   -> calculate(series, eomCacheService.getEom14(symbol, interval, series));

            default ->
                    throw new IllegalArgumentException("Frame EOM não suportado: " + frame);
        };
    }
}
