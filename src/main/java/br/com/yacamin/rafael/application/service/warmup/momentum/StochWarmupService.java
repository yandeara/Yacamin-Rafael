package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.StochCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.StochDerivation;
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
public class StochWarmupService {

    private static final double MID = 50.0;

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    private final StochCacheService stochCacheService;
    private final StochDerivation stochDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][STOCH] erro interno no calculo",
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

        log.info("[WARMUP][MOM][STOCH] {} - {}", symbol, openTime);

        var stoch14 = stochCacheService.getStoch14(symbol, interval, series);
        var k14 = stoch14.getK();
        var d14 = stoch14.getD();

        List<Callable<Void>> derivate = List.of(
                // DELTA (K/D)
                timedIfZero("mom_stoch_14_k_dlt", entity::getMom_stoch_14_k_dlt, () -> stochDerivation.calculateKDelta(k14, index), entity::setMom_stoch_14_k_dlt),
                timedIfZero("mom_stoch_14_d_dlt", entity::getMom_stoch_14_d_dlt, () -> stochDerivation.calculateDDelta(d14, index), entity::setMom_stoch_14_d_dlt),

                // SPREAD
                timedIfZero("mom_stoch_14_spread", entity::getMom_stoch_14_spread,
                        () -> stochDerivation.calculateSpread(stochDerivation.calculateK(k14, index), stochDerivation.calculateD(d14, index)),
                        entity::setMom_stoch_14_spread),

                // DIST MID (K)
                timedIfZero("mom_stoch_14_k_dst_mid", entity::getMom_stoch_14_k_dst_mid,
                        () -> stochDerivation.calculateKDistMid(k14, index, MID),
                        entity::setMom_stoch_14_k_dst_mid)
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
