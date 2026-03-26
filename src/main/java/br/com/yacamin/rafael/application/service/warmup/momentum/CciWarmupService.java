package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.CciCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.CciDerivation;
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
public class CciWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    private final CciCacheService cciCacheService;
    private final CciDerivation cciDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][CCI] erro interno no calculo",
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

        log.info("[WARMUP][MOM][CCI] {} - {}", symbol, openTime);

        var cci14  = cciCacheService.getCci14(symbol, interval, series).getIndicator();
        var cci20  = cciCacheService.getCci20(symbol, interval, series).getIndicator();

        List<Callable<Void>> derivate = List.of(
                // DELTA
                timedIfZero("mom_cci_14_dlt", entity::getMom_cci_14_dlt,
                        () -> cciDerivation.calculateDelta(cci14, index),
                        entity::setMom_cci_14_dlt),

                timedIfZero("mom_cci_20_dlt", entity::getMom_cci_20_dlt,
                        () -> cciDerivation.calculateDelta(cci20, index),
                        entity::setMom_cci_20_dlt),

                // DIST MID
                timedIfZero("mom_cci_14_dst_mid", entity::getMom_cci_14_dst_mid,
                        () -> cciDerivation.calculateDistMid(cci14, index),
                        entity::setMom_cci_14_dst_mid),

                timedIfZero("mom_cci_20_dst_mid", entity::getMom_cci_20_dst_mid,
                        () -> cciDerivation.calculateDistMid(cci20, index),
                        entity::setMom_cci_20_dst_mid)
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
