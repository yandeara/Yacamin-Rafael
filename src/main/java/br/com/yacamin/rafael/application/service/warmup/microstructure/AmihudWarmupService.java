package br.com.yacamin.rafael.application.service.warmup.microstructure;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.microstructure.AmihudDerivation;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MicrostructureIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Calcula APENAS os 6 campos Amihud usados na máscara getProdMask():
 * - mic_amihud_slp_w4, mic_amihud_slp_w20 (slope)
 * - mic_amihud_acc_w4, mic_amihud_acc_w5, mic_amihud_acc_w10, mic_amihud_acc_w16 (acceleration)
 *
 * Todos calculam direto do BarSeries, sem dependências intermediárias.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmihudWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(6);

    private final AmihudDerivation amihudDerivation;

    public void analyse(MicrostructureIndicatorEntity entity, SymbolCandle candle, BarSeries series) {

        var index = series.getEndIndex();

        List<Callable<Void>> tasks = List.of(
                timedIfZero("mic_amihud_slp_w4",
                        entity::getMic_amihud_slp_w4,
                        () -> amihudDerivation.calculateAmihudSlope(candle, series, index, 4),
                        entity::setMic_amihud_slp_w4),
                timedIfZero("mic_amihud_slp_w20",
                        entity::getMic_amihud_slp_w20,
                        () -> amihudDerivation.calculateAmihudSlope(candle, series, index, 20),
                        entity::setMic_amihud_slp_w20),
                timedIfZero("mic_amihud_acc_w4",
                        entity::getMic_amihud_acc_w4,
                        () -> amihudDerivation.calculateAmihudAcceleration(candle, series, index, 4),
                        entity::setMic_amihud_acc_w4),
                timedIfZero("mic_amihud_acc_w5",
                        entity::getMic_amihud_acc_w5,
                        () -> amihudDerivation.calculateAmihudAcceleration(candle, series, index, 5),
                        entity::setMic_amihud_acc_w5),
                timedIfZero("mic_amihud_acc_w10",
                        entity::getMic_amihud_acc_w10,
                        () -> amihudDerivation.calculateAmihudAcceleration(candle, series, index, 10),
                        entity::setMic_amihud_acc_w10),
                timedIfZero("mic_amihud_acc_w16",
                        entity::getMic_amihud_acc_w16,
                        () -> amihudDerivation.calculateAmihudAcceleration(candle, series, index, 16),
                        entity::setMic_amihud_acc_w16)
        );

        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try { f.get(); }
                catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][MIC][AMIHUD] erro", ee.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private Callable<Void> timedIfZero(String name, Supplier<Double> getter,
                                        Supplier<Double> calculator, Consumer<Double> setter) {
        return () -> {
            Double current = getter.get();
            if (current == null || current == 0) {
                setter.accept(DoubleValidator.validate(calculator.get(), name));
            }
            return null;
        };
    }
}
