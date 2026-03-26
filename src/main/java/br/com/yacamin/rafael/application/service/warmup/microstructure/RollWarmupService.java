package br.com.yacamin.rafael.application.service.warmup.microstructure;

import br.com.yacamin.rafael.application.service.indicator.microstructure.RollDerivation;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MicrostructureIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Calcula APENAS os 6 campos Roll usados na máscara:
 * mic_roll_spread_slp_w16/w32/w48, mic_roll_spread_acc_w16/w32/w48
 * Todos calculam direto do BarSeries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RollWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(6);
    private final RollDerivation rollDerivation;

    public void analyse(MicrostructureIndicatorEntity entity, SymbolCandle candle, BarSeries series) {
        var index = series.getEndIndex();

        List<Callable<Void>> tasks = List.of(
                calc("mic_roll_spread_slp_w16", entity::getMic_roll_spread_slp_w16,
                        () -> rollDerivation.calculateRollSpreadSlope(candle, series, 16, index, 20), entity::setMic_roll_spread_slp_w16),
                calc("mic_roll_spread_slp_w32", entity::getMic_roll_spread_slp_w32,
                        () -> rollDerivation.calculateRollSpreadSlope(candle, series, 32, index, 20), entity::setMic_roll_spread_slp_w32),
                calc("mic_roll_spread_slp_w48", entity::getMic_roll_spread_slp_w48,
                        () -> rollDerivation.calculateRollSpreadSlope(candle, series, 48, index, 20), entity::setMic_roll_spread_slp_w48),
                calc("mic_roll_spread_acc_w16", entity::getMic_roll_spread_acc_w16,
                        () -> rollDerivation.calculateSpreadAcceleration(candle, series, 16, index), entity::setMic_roll_spread_acc_w16),
                calc("mic_roll_spread_acc_w32", entity::getMic_roll_spread_acc_w32,
                        () -> rollDerivation.calculateSpreadAcceleration(candle, series, 32, index), entity::setMic_roll_spread_acc_w32),
                calc("mic_roll_spread_acc_w48", entity::getMic_roll_spread_acc_w48,
                        () -> rollDerivation.calculateSpreadAcceleration(candle, series, 48, index), entity::setMic_roll_spread_acc_w48)
        );

        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) { try { f.get(); } catch (ExecutionException ee) { throw new RuntimeException(ee.getCause()); } }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
    }

    private Callable<Void> calc(String name, Supplier<Double> getter, Supplier<Double> calculator, Consumer<Double> setter) {
        return () -> { Double c = getter.get(); if (c == null || c == 0) setter.accept(DoubleValidator.validate(calculator.get(), name)); return null; };
    }
}
