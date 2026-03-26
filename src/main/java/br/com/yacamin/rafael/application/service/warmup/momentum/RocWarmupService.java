package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.RocCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.RocDerivation;
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
public class RocWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    private final RocCacheService rocCacheService;
    private final RocDerivation rocDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][ROC] erro interno no calculo",
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

        log.info("[WARMUP][MOM][ROC] {} - {}", symbol, openTime);

        var roc1 = rocCacheService.getRoc1(symbol, interval, series).getIndicator();
        var roc2 = rocCacheService.getRoc2(symbol, interval, series).getIndicator();
        var roc3 = rocCacheService.getRoc3(symbol, interval, series).getIndicator();
        var roc5 = rocCacheService.getRoc5(symbol, interval, series).getIndicator();

        List<Callable<Void>> derivate = List.of(
                // RAW
                timedIfZero("mom_roc_1", entity::getMom_roc_1, () -> rocDerivation.calculateRaw(roc1, index), entity::setMom_roc_1),
                timedIfZero("mom_roc_2", entity::getMom_roc_2, () -> rocDerivation.calculateRaw(roc2, index), entity::setMom_roc_2),
                timedIfZero("mom_roc_3", entity::getMom_roc_3, () -> rocDerivation.calculateRaw(roc3, index), entity::setMom_roc_3),
                timedIfZero("mom_roc_5", entity::getMom_roc_5, () -> rocDerivation.calculateRaw(roc5, index), entity::setMom_roc_5)
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
            if (current == null || current == 0d) {
                setter.accept(DoubleValidator.validate(calculator.get(), name));
            }
        });
    }
}
