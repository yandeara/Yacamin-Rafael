package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.CmoCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.CmoDerivation;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MomentumIndicatorEntity;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
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
public class CmoWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    private final CmoCacheService cmoCacheService;
    private final CmoDerivation cmoDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][CMO] erro interno no calculo",
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

        log.info("[WARMUP][MOM][CMO] {} - {}", symbol, openTime);

        var cmo14  = cmoCacheService.getCmo14(symbol, interval, series);
        var cmo20  = cmoCacheService.getCmo20(symbol, interval, series);

        List<Callable<Void>> derivate = List.of(
                // DELTA
                timedIfZero("mom_cmo_14_dlt", entity::getMom_cmo_14_dlt,
                        () -> cmoDerivation.calculateDelta(cmo14, index),
                        entity::setMom_cmo_14_dlt),

                timedIfZero("mom_cmo_20_dlt", entity::getMom_cmo_20_dlt,
                        () -> cmoDerivation.calculateDelta(cmo20, index),
                        entity::setMom_cmo_20_dlt),

                // DIST MID
                timedIfZero("mom_cmo_14_dst_mid", entity::getMom_cmo_14_dst_mid,
                        () -> cmoDerivation.calculateDistMid(cmo14, index),
                        entity::setMom_cmo_14_dst_mid),

                timedIfZero("mom_cmo_20_dst_mid", entity::getMom_cmo_20_dst_mid,
                        () -> cmoDerivation.calculateDistMid(cmo20, index),
                        entity::setMom_cmo_20_dst_mid)
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
