package br.com.yacamin.rafael.application.service.warmup.volume;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.DeltaDerivation;
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
public class VolumeDeltaWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(6);

    private final DeltaDerivation deltaDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VOL][DELTA] erro interno no cálculo", ee.getCause());
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
        var openTime = candle.getOpenTime();

        log.info("[WARMUP][VOL][DELTA] {} - {}", symbol, openTime);

        List<Callable<Void>> tasks = List.of(

                timedIfZero("vol_volume_delta_1",
                        entity::getVol_volume_delta_1,
                        () -> deltaDerivation.volumeDelta(series, 1),
                        entity::setVol_volume_delta_1
                ),

                timedIfZero("vol_volume_delta_3",
                        entity::getVol_volume_delta_3,
                        () -> deltaDerivation.volumeDelta(series, 3),
                        entity::setVol_volume_delta_3
                ),

                timedIfZero("vol_trades_delta_1",
                        entity::getVol_trades_delta_1,
                        () -> deltaDerivation.tradesDelta(series, 1),
                        entity::setVol_trades_delta_1
                ),

                timedIfZero("vol_trades_delta_3",
                        entity::getVol_trades_delta_3,
                        () -> deltaDerivation.tradesDelta(series, 3),
                        entity::setVol_trades_delta_3
                ),

                timedIfZero("vol_quote_volume_delta_1",
                        entity::getVol_quote_volume_delta_1,
                        () -> deltaDerivation.quoteVolumeDelta(series, 1),
                        entity::setVol_quote_volume_delta_1
                ),

                timedIfZero("vol_quote_volume_delta_3",
                        entity::getVol_quote_volume_delta_3,
                        () -> deltaDerivation.quoteVolumeDelta(series, 3),
                        entity::setVol_quote_volume_delta_3
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
