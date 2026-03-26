package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.RocCacheService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ROCIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocIndicatorService {

    private final RocCacheService rocCacheService;

    private double compute(ROCIndicator indicator, int last) {
        return indicator.getValue(last).doubleValue();
    }

    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        return switch (frame) {

            case ROC_1 -> compute(
                    rocCacheService.getRoc1(symbol, interval, series).getIndicator(),
                    last
            );

            case ROC_2 -> compute(
                    rocCacheService.getRoc2(symbol, interval, series).getIndicator(),
                    last
            );

            case ROC_3 -> compute(
                    rocCacheService.getRoc3(symbol, interval, series).getIndicator(),
                    last
            );

            case ROC_5 -> compute(
                    rocCacheService.getRoc5(symbol, interval, series).getIndicator(),
                    last
            );

            default ->
                    throw new IllegalArgumentException("Frame ROC não suportado: " + frame);
        };
    }
}
