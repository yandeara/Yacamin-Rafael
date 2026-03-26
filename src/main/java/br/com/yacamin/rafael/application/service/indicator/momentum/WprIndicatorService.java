package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.WprCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.WilliamsRIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class WprIndicatorService {

    private final WprCacheService wprCacheService;

    // =============================================================================================
    // WPR bruto
    // =============================================================================================
    private double compute(WilliamsRIndicator indicator, int last) {
        return indicator.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Delta = WPR_t - WPR_{t-1}
    // =============================================================================================
    private double computeDelta(WilliamsRIndicator indicator, int last) {
        double curr = indicator.getValue(last).doubleValue();
        double prev = indicator.getValue(last - 1).doubleValue();
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

            case WPR_14 -> compute(
                    wprCacheService.getWpr14(symbol, interval, series).getIndicator(),
                    last
            );

            case WPR_28 -> compute(
                    wprCacheService.getWpr28(symbol, interval, series).getIndicator(),
                    last
            );

            case WPR_42 -> compute(
                    wprCacheService.getWpr42(symbol, interval, series).getIndicator(),
                    last
            );

            case WPR_14_DLT -> computeDelta(
                    wprCacheService.getWpr14(symbol, interval, series).getIndicator(),
                    last
            );

            case WPR_28_DLT -> computeDelta(
                    wprCacheService.getWpr28(symbol, interval, series).getIndicator(),
                    last
            );

            case WPR_42_DLT -> computeDelta(
                    wprCacheService.getWpr42(symbol, interval, series).getIndicator(),
                    last
            );

            default ->
                    throw new IllegalArgumentException("Frame WPR não suportado: " + frame);
        };
    }
}
