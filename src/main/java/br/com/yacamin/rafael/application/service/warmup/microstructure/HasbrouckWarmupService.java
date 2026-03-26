package br.com.yacamin.rafael.application.service.warmup.microstructure;

import br.com.yacamin.rafael.application.service.indicator.microstructure.HasbrouckDerivation;
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
 * Calcula APENAS os 2 campos Hasbrouck usados na máscara:
 * - mic_hasb_lambda_w16_slp_w20
 * - mic_hasb_lambda_w48_slp_w20
 * Ambos calculam direto do BarSeries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HasbrouckWarmupService {

    private final HasbrouckDerivation hasbDerivation;

    public void analyse(MicrostructureIndicatorEntity entity, SymbolCandle candle, BarSeries series) {
        var index = series.getEndIndex();

        List<Callable<Void>> tasks = List.of(
                timedIfZero("mic_hasb_lambda_w16_slp_w20", entity::getMic_hasb_lambda_w16_slp_w20,
                        () -> hasbDerivation.calculateHasbSlopeW20(candle, series, 16, index),
                        entity::setMic_hasb_lambda_w16_slp_w20),
                timedIfZero("mic_hasb_lambda_w48_slp_w20", entity::getMic_hasb_lambda_w48_slp_w20,
                        () -> hasbDerivation.calculateHasbSlopeW20(candle, series, 48, index),
                        entity::setMic_hasb_lambda_w48_slp_w20)
        );

        try {
            var pool = Executors.newFixedThreadPool(2);
            var futures = pool.invokeAll(tasks);
            for (var f : futures) { try { f.get(); } catch (ExecutionException ee) { throw new RuntimeException(ee.getCause()); } }
            pool.shutdown();
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
    }

    private Callable<Void> timedIfZero(String name, Supplier<Double> getter, Supplier<Double> calc, Consumer<Double> setter) {
        return () -> { Double c = getter.get(); if (c == null || c == 0) setter.accept(DoubleValidator.validate(calc.get(), name)); return null; };
    }
}
