package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.TrixCacheService;
import br.com.yacamin.rafael.application.service.indicator.extension.TrixIndicator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrixIndicatorService {

    private final TrixCacheService trixCacheService;

    // =============================================================================================
    // TRIX puro
    // =============================================================================================
    private double compute(TrixIndicator trix, int last) {
        return trix.getValue(last).doubleValue();
    }

    // =============================================================================================
    // TRIX Delta = TRIX_t - TRIX_{t-1}
    // =============================================================================================
    private double computeDelta(TrixIndicator trix, int last) {
        double curr = trix.getValue(last).doubleValue();
        double prev = trix.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval = candle.getInterval();
        int last = series.getEndIndex();

        return switch (frame) {

            case TRIX_9 -> {
                TrixIndicator trix = trixCacheService.getTrix9(symbol, interval, series);
                yield compute(trix, last);
            }

            case TRIX_9_DLT -> {
                TrixIndicator trix = trixCacheService.getTrix9(symbol, interval, series);
                yield computeDelta(trix, last);
            }

            default ->
                    throw new IllegalArgumentException("Frame TRIX não suportado: " + frame);
        };
    }
}
