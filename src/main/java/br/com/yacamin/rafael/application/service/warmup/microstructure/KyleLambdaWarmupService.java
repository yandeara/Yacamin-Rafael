package br.com.yacamin.rafael.application.service.warmup.microstructure;

import br.com.yacamin.rafael.application.service.indicator.microstructure.KyleDerivation;
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
 * Calcula APENAS os 12 campos Kyle Lambda usados na máscara:
 * w4: slp_w4, slp_w20, acc_w4, acc_w5, acc_w10, acc_w16
 * w16: slp_w4, slp_w20, acc_w4, acc_w5, acc_w10, acc_w16
 * Todos calculam direto do BarSeries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KyleLambdaWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(12);
    private final KyleDerivation kyleDerivation;

    public void analyse(MicrostructureIndicatorEntity entity, SymbolCandle candle, BarSeries series) {
        var index = series.getEndIndex();

        List<Callable<Void>> tasks = List.of(
                calc("mic_kyle_lambda_w4_slp_w4", entity::getMic_kyle_lambda_w4_slp_w4,
                        () -> kyleDerivation.calculateKyleSlope(candle, series, 4, index, 4), entity::setMic_kyle_lambda_w4_slp_w4),
                calc("mic_kyle_lambda_w4_slp_w20", entity::getMic_kyle_lambda_w4_slp_w20,
                        () -> kyleDerivation.calculateKyleSlope(candle, series, 4, index, 20), entity::setMic_kyle_lambda_w4_slp_w20),
                calc("mic_kyle_lambda_w4_acc_w4", entity::getMic_kyle_lambda_w4_acc_w4,
                        () -> kyleDerivation.calculateKyleAcceleration(candle, series, 4, index, 4), entity::setMic_kyle_lambda_w4_acc_w4),
                calc("mic_kyle_lambda_w4_acc_w5", entity::getMic_kyle_lambda_w4_acc_w5,
                        () -> kyleDerivation.calculateKyleAcceleration(candle, series, 4, index, 5), entity::setMic_kyle_lambda_w4_acc_w5),
                calc("mic_kyle_lambda_w4_acc_w10", entity::getMic_kyle_lambda_w4_acc_w10,
                        () -> kyleDerivation.calculateKyleAcceleration(candle, series, 4, index, 10), entity::setMic_kyle_lambda_w4_acc_w10),
                calc("mic_kyle_lambda_w4_acc_w16", entity::getMic_kyle_lambda_w4_acc_w16,
                        () -> kyleDerivation.calculateKyleAcceleration(candle, series, 4, index, 16), entity::setMic_kyle_lambda_w4_acc_w16),
                calc("mic_kyle_lambda_w16_slp_w4", entity::getMic_kyle_lambda_w16_slp_w4,
                        () -> kyleDerivation.calculateKyleSlope(candle, series, 16, index, 4), entity::setMic_kyle_lambda_w16_slp_w4),
                calc("mic_kyle_lambda_w16_slp_w20", entity::getMic_kyle_lambda_w16_slp_w20,
                        () -> kyleDerivation.calculateKyleSlope(candle, series, 16, index, 20), entity::setMic_kyle_lambda_w16_slp_w20),
                calc("mic_kyle_lambda_w16_acc_w4", entity::getMic_kyle_lambda_w16_acc_w4,
                        () -> kyleDerivation.calculateKyleAcceleration(candle, series, 16, index, 4), entity::setMic_kyle_lambda_w16_acc_w4),
                calc("mic_kyle_lambda_w16_acc_w5", entity::getMic_kyle_lambda_w16_acc_w5,
                        () -> kyleDerivation.calculateKyleAcceleration(candle, series, 16, index, 5), entity::setMic_kyle_lambda_w16_acc_w5),
                calc("mic_kyle_lambda_w16_acc_w10", entity::getMic_kyle_lambda_w16_acc_w10,
                        () -> kyleDerivation.calculateKyleAcceleration(candle, series, 16, index, 10), entity::setMic_kyle_lambda_w16_acc_w10),
                calc("mic_kyle_lambda_w16_acc_w16", entity::getMic_kyle_lambda_w16_acc_w16,
                        () -> kyleDerivation.calculateKyleAcceleration(candle, series, 16, index, 16), entity::setMic_kyle_lambda_w16_acc_w16)
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
