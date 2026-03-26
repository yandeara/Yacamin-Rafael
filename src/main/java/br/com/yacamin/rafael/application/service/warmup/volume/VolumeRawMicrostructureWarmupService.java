package br.com.yacamin.rafael.application.service.warmup.volume;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volume.VolumeMicrostructureDerivation;
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
public class VolumeRawMicrostructureWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(5);

    private final VolumeMicrostructureDerivation volumeMicrostructureDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][VOL][RAW] erro interno no cálculo",
                            ee.getCause()
                    );
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
        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var index = series.getEndIndex();

        log.info("[WARMUP][VOL][RAW] {} - {}", symbol, openTime);

        List<Callable<Void>> tasks = List.of(

                // RAW MASK FEATURES
                timed("vol_taker_buy_ratio",
                        () -> entity.setVol_taker_buy_ratio(
                                DoubleValidator.validate(volumeMicrostructureDerivation.takerBuyRatio(candle), "vol_taker_buy_ratio")
                        )
                ),

                timed("vol_taker_buy_sell_imbalance",
                        () -> entity.setVol_taker_buy_sell_imbalance(
                                DoubleValidator.validate(volumeMicrostructureDerivation.takerBuySellImbalance(candle), "vol_taker_buy_sell_imbalance")
                        )
                ),

                // TAKER PRESSURE DYNAMICS — MASK FEATURES
                timedIfZero("vol_taker_buy_ratio_rel_16",
                        entity::getVol_taker_buy_ratio_rel_16,
                        () -> volumeMicrostructureDerivation.takerBuyRatioRel(series, index, 16),
                        entity::setVol_taker_buy_ratio_rel_16),

                timedIfZero("vol_taker_buy_ratio_slp_w20",
                        entity::getVol_taker_buy_ratio_slp_w20,
                        () -> volumeMicrostructureDerivation.takerBuyRatioSlpW20(series, index),
                        entity::setVol_taker_buy_ratio_slp_w20),

                timedIfZero("vol_taker_buy_sell_imbalance_slp_w20",
                        entity::getVol_taker_buy_sell_imbalance_slp_w20,
                        () -> volumeMicrostructureDerivation.takerImbalanceSlpW20(series, index),
                        entity::setVol_taker_buy_sell_imbalance_slp_w20)
        );

        execute(tasks);
    }

    private Callable<Void> timed(String name, Runnable task) {
        return () -> {
            task.run();
            return null;
        };
    }

    private Callable<Void> timedIfZero(String name,
                                       Supplier<Double> getter,
                                       Supplier<Double> calc,
                                       Consumer<Double> setter) {
        return () -> {
            Double cur = getter.get();
            if (cur == null || cur == 0d) {
                setter.accept(DoubleValidator.validate(calc.get(), name));
            }
            return null;
        };
    }

}
