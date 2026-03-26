package br.com.yacamin.rafael.application.service.indicator.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.StochCacheService;
import br.com.yacamin.rafael.application.service.indicator.momentum.dto.StochDto;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.Frame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

@Slf4j
@Service
@RequiredArgsConstructor
public class StochIndicatorService {

    private final StochCacheService stochCacheService;

    // =============================================================================================
    // %K puro
    // =============================================================================================
    private double computeK(BarSeries series, StochasticOscillatorKIndicator k) {
        int last = series.getEndIndex();
        return k.getValue(last).doubleValue();
    }

    // =============================================================================================
    // %D puro
    // =============================================================================================
    private double computeD(BarSeries series, StochasticOscillatorDIndicator d) {
        int last = series.getEndIndex();
        return d.getValue(last).doubleValue();
    }

    // =============================================================================================
    // Delta K = K_t - K_{t-1}
    // =============================================================================================
    private double computeKDlt(BarSeries series, StochasticOscillatorKIndicator k) {
        int last = series.getEndIndex();
        double curr = k.getValue(last).doubleValue();
        double prev = k.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // Delta D = D_t - D_{t-1}
    // =============================================================================================
    private double computeDDlt(BarSeries series, StochasticOscillatorDIndicator d) {
        int last = series.getEndIndex();
        double curr = d.getValue(last).doubleValue();
        double prev = d.getValue(last - 1).doubleValue();
        return curr - prev;
    }

    // =============================================================================================
    // Opcional: computar o par K/D (DTO deve ser double)
    // =============================================================================================
    private StochDto computePair(StochasticOscillatorKIndicator k,
                                 StochasticOscillatorDIndicator d,
                                 int last) {

        // Sem try/catch. Se der erro → explode (Regra de Ouro)
        double kVal = k.getValue(last).doubleValue();
        double dVal = d.getValue(last).doubleValue();

        return StochDto.builder()
                .k(kVal)
                .d(dVal)
                .build();
    }

    // =============================================================================================
    // Dispatcher principal
    // =============================================================================================
    public double calculate(BarSeries series, SymbolCandle candle, Frame frame) {

        String symbol = candle.getSymbol();
        var interval  = candle.getInterval();
        int last      = series.getEndIndex();

        return switch (frame) {

            case STOCH_5_K -> {
                var dto = stochCacheService.getStoch5(symbol, interval, series);
                yield computeK(series, dto.getK());
            }

            case STOCH_5_D -> {
                var dto = stochCacheService.getStoch5(symbol, interval, series);
                yield computeD(series, dto.getD());
            }

            case STOCH_5_K_DLT -> {
                var dto = stochCacheService.getStoch5(symbol, interval, series);
                yield computeKDlt(series, dto.getK());
            }

            case STOCH_5_D_DLT -> {
                var dto = stochCacheService.getStoch5(symbol, interval, series);
                yield computeDDlt(series, dto.getD());
            }

            case STOCH_14_K -> {
                var dto = stochCacheService.getStoch14(symbol, interval, series);
                yield computeK(series, dto.getK());
            }

            case STOCH_14_D -> {
                var dto = stochCacheService.getStoch14(symbol, interval, series);
                yield computeD(series, dto.getD());
            }

            case STOCH_14_K_DLT -> {
                var dto = stochCacheService.getStoch14(symbol, interval, series);
                yield computeKDlt(series, dto.getK());
            }

            case STOCH_14_D_DLT -> {
                var dto = stochCacheService.getStoch14(symbol, interval, series);
                yield computeDDlt(series, dto.getD());
            }

            default ->
                    throw new IllegalArgumentException("Frame STOCH não suportado: " + frame);
        };
    }
}
