package br.com.yacamin.rafael.application.service.warmup.volume;

import br.com.yacamin.rafael.application.service.cache.indicator.OfiCacheService;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.VolumeIndicatorEntity;
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
public class VolumeOfiWarmupService {

    private static final int W20 = 20;

    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    private final OfiCacheService ofiCacheService;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VOL][OFI] erro interno no cálculo", ee.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void analyse(
            VolumeIndicatorEntity entity,
            SymbolCandle candle,
            BarSeries series
    ) {

        var symbol   = candle.getSymbol();
        var interval = candle.getInterval();
        var openTime = candle.getOpenTime();
        int end      = series.getEndIndex();

        log.info("[WARMUP][VOL][OFI] {} - {}", symbol, openTime);

        List<Callable<Void>> tasks = List.of(

                timedIfZero("vol_ofi",
                        entity::getVol_ofi,
                        () -> ofiCacheService.getOfiRaw(symbol, interval, series).getValue(end).doubleValue(),
                        entity::setVol_ofi
                ),

                timedIfZero("vol_ofi_rel_16",
                        entity::getVol_ofi_rel_16,
                        () -> ofiCacheService.getOfiRel(symbol, interval, series, 16).getValue(end).doubleValue(),
                        entity::setVol_ofi_rel_16
                ),

                timedIfZero("vol_ofi_slp_w20",
                        entity::getVol_ofi_slp_w20,
                        () -> ofiCacheService.getOfiSlope(symbol, interval, series, W20).getValue(end).doubleValue(),
                        entity::setVol_ofi_slp_w20
                )
        );

        execute(tasks);
    }

    private Callable<Void> timedIfZero(
            String name,
            Supplier<Double> getter,
            Supplier<Double> calc,
            Consumer<Double> setter
    ) {
        return () -> {
            Double cur = getter.get();
            if (cur == null || cur == 0) {
                setter.accept(DoubleValidator.validate(calc.get(), name));
            }
            return null;
        };
    }
}
