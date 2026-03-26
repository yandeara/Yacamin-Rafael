package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.TrixCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.TrixDerivation;
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
public class TrixWarmupService {

    private static final int SIG_PERIOD = 9;

    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    private final TrixCacheService trixCacheService;
    private final TrixDerivation trixDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][TRIX] erro interno no calculo",
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

        log.info("[WARMUP][MOM][TRIX] {} - {}", symbol, openTime);

        var trix9 = trixCacheService.getTrix9(symbol, interval, series);

        List<Callable<Void>> derivate = List.of(
                // DELTA
                timedIfZero("mom_trix_9_dlt", entity::getMom_trix_9_dlt, () -> trixDerivation.calculateDelta(trix9, index), entity::setMom_trix_9_dlt),

                // HIST (TRIX - signal)
                timedIfZero("mom_trix_9_hist", entity::getMom_trix_9_hist, () -> trixDerivation.calculateHist(trix9, index, SIG_PERIOD), entity::setMom_trix_9_hist)
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
