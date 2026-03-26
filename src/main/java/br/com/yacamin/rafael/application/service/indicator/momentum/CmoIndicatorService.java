package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.CmoCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CMOIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmoIndicatorService {

    private final CmoCacheService cmoCacheService;

    // =============================================================================================
    // CMO puro
    // =============================================================================================
    private double compute(CMOIndicator cmo, int last) {
        return cmo.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Delta = CMO_t - CMO_{t-1}
    // =============================================================================================
    private double computeDelta(CMOIndicator cmo, int last) {
        double curr = cmo.getValue(last).doubleValue();
        double prev = cmo.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // Dispatcher
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        return switch (frame) {

            case CMO_14     -> compute(cmoCacheService.getCmo14(symbol, interval, series), last);
            case CMO_20     -> compute(cmoCacheService.getCmo20(symbol, interval, series), last);

            case CMO_14_DLT -> computeDelta(cmoCacheService.getCmo14(symbol, interval, series), last);
            case CMO_20_DLT -> computeDelta(cmoCacheService.getCmo20(symbol, interval, series), last);

            default ->
                    throw new IllegalArgumentException("Frame CMO não suportado: " + frame);
        };
    }
}
