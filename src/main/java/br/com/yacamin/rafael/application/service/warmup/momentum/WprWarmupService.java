package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.WprCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.WprDerivation;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MomentumIndicatorEntity;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class WprWarmupService {

    private static final double MID = -50.0;

    private final ExecutorService pool = Executors.newFixedThreadPool(6);

    private final WprCacheService wprCacheService;
    private final WprDerivation wprDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][WPR] erro interno no calculo",
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

        log.info("[WARMUP][MOM][WPR] {} - {}", symbol, openTime);

        var wpr14  = wprCacheService.getWpr14(symbol, interval, series).getIndicator();
        var wpr28  = wprCacheService.getWpr28(symbol, interval, series).getIndicator();
        var wpr42  = wprCacheService.getWpr42(symbol, interval, series).getIndicator();
        var wpr48  = wprCacheService.getWpr48(symbol, interval, series).getIndicator();

        List<Callable<Void>> derivate = List.of(
                // DELTA
                timedIfZero("mom_wpr_14_dlt", entity::getMom_wpr_14_dlt, () -> wprDerivation.calculateDelta(wpr14, index), entity::setMom_wpr_14_dlt),
                timedIfZero("mom_wpr_28_dlt", entity::getMom_wpr_28_dlt, () -> wprDerivation.calculateDelta(wpr28, index), entity::setMom_wpr_28_dlt),
                timedIfZero("mom_wpr_42_dlt", entity::getMom_wpr_42_dlt, () -> wprDerivation.calculateDelta(wpr42, index), entity::setMom_wpr_42_dlt),

                // DIST MID
                timedIfZero("mom_wpr_14_dst_mid", entity::getMom_wpr_14_dst_mid, () -> wprDerivation.calculateDistMid(wpr14, index, MID), entity::setMom_wpr_14_dst_mid),
                timedIfZero("mom_wpr_28_dst_mid", entity::getMom_wpr_28_dst_mid, () -> wprDerivation.calculateDistMid(wpr28, index, MID), entity::setMom_wpr_28_dst_mid),
                timedIfZero("mom_wpr_48_dst_mid", entity::getMom_wpr_48_dst_mid, () -> wprDerivation.calculateDistMid(wpr48, index, MID), entity::setMom_wpr_48_dst_mid)
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
