package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.TsiCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.TsiDerivation;
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
public class TsiWarmupService {

    private static final int SIG_PERIOD = 7;

    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    private final TsiCacheService tsiCacheService;
    private final TsiDerivation tsiDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][TSI] erro interno no calculo",
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

        log.info("[WARMUP][MOM][TSI] {} - {}", symbol, openTime);

        var tsi25_13 = tsiCacheService.getTsi25_13(symbol, interval, series);

        List<Callable<Void>> derivate = List.of(
                // DELTA
                timedIfZero("mom_tsi_25_13_dlt", entity::getMom_tsi_25_13_dlt, () -> tsiDerivation.calculateDelta(tsi25_13, index), entity::setMom_tsi_25_13_dlt),

                // HIST (TSI - signal)
                timedIfZero("mom_tsi_25_13_hist", entity::getMom_tsi_25_13_hist, () -> tsiDerivation.calculateHist(tsi25_13, index, SIG_PERIOD), entity::setMom_tsi_25_13_hist),

                // DIST MID
                timedIfZero("mom_tsi_25_13_dst_mid", entity::getMom_tsi_25_13_dst_mid, () -> tsiDerivation.calculateDistMid(tsi25_13, index), entity::setMom_tsi_25_13_dst_mid)
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
