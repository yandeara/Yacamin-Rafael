package br.com.yacamin.rafael.application.service.warmup.volume;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volume.MicroburstDerivation;
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
public class VolumeMicroburstWarmupService {

    private static final int P16 = 16;

    private final ExecutorService pool = Executors.newFixedThreadPool(5);

    private final MicroburstDerivation microburst;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VOL][MICROBURST] erro interno no cálculo", ee.getCause());
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

        log.info("[WARMUP][VOL][MICROBURST] {} - {}", symbol, openTime);

        List<Callable<Void>> tasks = List.of(

                // SPIKE SCORE (16)
                timedIfZero("vol_volume_spike_score_16",
                        entity::getVol_volume_spike_score_16,
                        () -> microburst.spikeScore(series, P16, MicroburstDerivation.VOLUME),
                        entity::setVol_volume_spike_score_16
                ),

                timedIfZero("vol_trades_spike_score_16",
                        entity::getVol_trades_spike_score_16,
                        () -> microburst.spikeScore(series, P16, MicroburstDerivation.TRADES),
                        entity::setVol_trades_spike_score_16
                ),

                // INTENSITY (16)
                timedIfZero("vol_microburst_volume_intensity_16",
                        entity::getVol_microburst_volume_intensity_16,
                        () -> microburst.intensity(series, P16, MicroburstDerivation.VOLUME),
                        entity::setVol_microburst_volume_intensity_16
                ),

                timedIfZero("vol_microburst_trades_intensity_16",
                        entity::getVol_microburst_trades_intensity_16,
                        () -> microburst.intensity(series, P16, MicroburstDerivation.TRADES),
                        entity::setVol_microburst_trades_intensity_16
                ),

                // COMBO (16)
                timedIfZero("vol_microburst_combo_16",
                        entity::getVol_microburst_combo_16,
                        () -> microburst.combo(series, P16),
                        entity::setVol_microburst_combo_16
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
