package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.TsiCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.TsiIndicator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class TsiIndicatorService {

    private final TsiCacheService tsiCacheService;

    // =============================================================================================
    // TSI puro
    // =============================================================================================
    private double compute(TsiIndicator tsi, int last) {
        return tsi.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Delta = TSI_t - TSI_{t-1}
    // =============================================================================================
    private double computeDelta(TsiIndicator tsi, int last) {

        double curr = tsi.getValue(last).doubleValue();
        double prev = tsi.getValue(last - 1).doubleValue();

        return curr - prev;
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol  = candle.getSymbol();
        var interval   = candle.getInterval();
        int last       = series.getEndIndex();

        return switch (frame) {

            case TSI_25_13 -> {
                TsiIndicator tsi = tsiCacheService.getTsi25_13(symbol, interval, series);
                yield compute(tsi, last);
            }

            case TSI_25_13_DLT -> {
                TsiIndicator tsi = tsiCacheService.getTsi25_13(symbol, interval, series);
                yield computeDelta(tsi, last);
            }

            default ->
                    throw new IllegalArgumentException("Frame TSI não suportado: " + frame);
        };
    }
}
