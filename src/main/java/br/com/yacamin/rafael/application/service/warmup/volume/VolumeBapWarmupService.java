package br.com.yacamin.rafael.application.service.warmup.volume;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volume.BidAskPressureDerivation;
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
public class VolumeBapWarmupService {

    private static final int W16 = 16;

    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    private final BidAskPressureDerivation bapDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VOL][BAP] erro interno no cálculo", ee.getCause());
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
        int end      = series.getEndIndex();

        log.info("[WARMUP][VOL][BAP] {} - {}", symbol, openTime);

        List<Callable<Void>> tasks = List.of(

                timedIfZero("vol_bap",
                        entity::getVol_bap,
                        () -> bapDerivation.bap(series, end),
                        entity::setVol_bap
                ),

                timedIfZero("vol_bap_slope_16",
                        entity::getVol_bap_slope_16,
                        () -> bapDerivation.bapSlope(series, end, W16),
                        entity::setVol_bap_slope_16
                ),

                timedIfZero("vol_bap_acc_16",
                        entity::getVol_bap_acc_16,
                        () -> bapDerivation.bapAcceleration(series, end, W16),
                        entity::setVol_bap_acc_16
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
