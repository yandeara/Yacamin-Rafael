package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.CciCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Slf4j
@Service
@RequiredArgsConstructor
public class CciIndicatorService {

    private final CciCacheService cciCacheService;

    public double calculate(BarSeries series,
                            SymbolCandle candle,
                            Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        return switch (frame) {

            case CCI_14 -> {
                var ind = cciCacheService.getCci14(symbol, interval, series).getIndicator();
                yield ind.getValue(last).doubleValue();
            }

            case CCI_20 -> {
                var ind = cciCacheService.getCci20(symbol, interval, series).getIndicator();
                yield ind.getValue(last).doubleValue();
            }

            default ->
                    throw new IllegalArgumentException("Frame CCI não suportado: " + frame);
        };
    }
}
