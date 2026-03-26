package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.PpoCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.PpoDerivation;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MomentumIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class PpoWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    private final PpoCacheService ppoCacheService;
    private final PpoDerivation ppoDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][PPO] erro interno no calculo",
                            ee.getCause()
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void analyse(MomentumIndicatorEntity entity,
                        SymbolCandle candle,
                        BarSeries series) {

        var symbol   = candle.getSymbol();
        var interval = candle.getInterval();
        var openTime = candle.getOpenTime();
        var index    = series.getEndIndex();

        log.info("[WARMUP][MOM][PPO] {} - {}", symbol, openTime);

        var ppoDto = ppoCacheService.getPpoDefault(symbol, interval, series);
        var ppo    = ppoDto.getPpo();
        var signal = ppoDto.getSignal();

        List<Callable<Void>> derivate = List.of(
                // HIST
                timedIfZero("mom_ppo_hist_12_26_9", entity::getMom_ppo_hist_12_26_9,
                        () -> ppoDerivation.calculateHistogram(ppo, signal, index),
                        entity::setMom_ppo_hist_12_26_9),

                // PPO DELTA
                timedIfZero("mom_ppo_12_26_dlt", entity::getMom_ppo_12_26_dlt,
                        () -> ppoDerivation.calculateDelta(ppo, index),
                        entity::setMom_ppo_12_26_dlt),

                // HIST DELTA
                timedIfZero("mom_ppo_hist_12_26_9_dlt", entity::getMom_ppo_hist_12_26_9_dlt,
                        () -> ppoDerivation.calculateHistogramDelta(ppo, signal, index),
                        entity::setMom_ppo_hist_12_26_9_dlt)
        );

        execute(derivate);
    }

    private Callable<Void> timed(String name, Runnable task) {
        return () -> {
            try {
                task.run();
                return null;
            } finally {
                // noop
            }
        };
    }

    private Callable<Void> timedIfZero(
            String name,
            Supplier<Double> getter,
            Supplier<Double> calculator,
            Consumer<Double> setter
    ) {
        return timed(name, () -> {
            Double current = getter.get();
            if (current == null || current == 0) {
                setter.accept(DoubleValidator.validate(calculator.get(), name));
            }
        });
    }
}
