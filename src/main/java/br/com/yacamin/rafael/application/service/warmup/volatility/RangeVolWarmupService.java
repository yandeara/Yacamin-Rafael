package br.com.yacamin.rafael.application.service.warmup.volatility;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volatility.RangeVolDerivation;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.VolatilityIndicatorEntity;
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
public class RangeVolWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    private final RangeVolDerivation rangeVolDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VLT][RANGEVOL] erro interno no calculo", ee.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void analyse(VolatilityIndicatorEntity entity, SymbolCandle candle, BarSeries series) {

        var symbol   = candle.getSymbol();
        var openTime = candle.getOpenTime();
        int last     = series.getEndIndex();

        log.info("[WARMUP][VLT][RANGEVOL] {} - {}", symbol, openTime);

        final int SLP_WINDOW = 20;

        List<Callable<Void>> tasks = List.of(

                // GK SLOPES (2)
                timedIfZero("vlt_vol_gk_16_slp", entity::getVlt_vol_gk_16_slp,
                        () -> rangeVolDerivation.gkSlope(series, last, 16, SLP_WINDOW),
                        entity::setVlt_vol_gk_16_slp),

                timedIfZero("vlt_vol_gk_32_slp", entity::getVlt_vol_gk_32_slp,
                        () -> rangeVolDerivation.gkSlope(series, last, 32, SLP_WINDOW),
                        entity::setVlt_vol_gk_32_slp),

                // PARK SLOPES (2)
                timedIfZero("vlt_vol_park_16_slp", entity::getVlt_vol_park_16_slp,
                        () -> rangeVolDerivation.parkSlope(series, last, 16, SLP_WINDOW),
                        entity::setVlt_vol_park_16_slp),

                timedIfZero("vlt_vol_park_32_slp", entity::getVlt_vol_park_32_slp,
                        () -> rangeVolDerivation.parkSlope(series, last, 32, SLP_WINDOW),
                        entity::setVlt_vol_park_32_slp),

                // RS SLOPES (2)
                timedIfZero("vlt_vol_rs_16_slp", entity::getVlt_vol_rs_16_slp,
                        () -> rangeVolDerivation.rsSlope(series, last, 16, SLP_WINDOW),
                        entity::setVlt_vol_rs_16_slp),

                timedIfZero("vlt_vol_rs_32_slp", entity::getVlt_vol_rs_32_slp,
                        () -> rangeVolDerivation.rsSlope(series, last, 32, SLP_WINDOW),
                        entity::setVlt_vol_rs_32_slp),

                // GK RATIOS (2)
                timedIfZero("vlt_vol_gk_16_48_ratio", entity::getVlt_vol_gk_16_48_ratio,
                        () -> rangeVolDerivation.gkRatio(series, last, 16, 48),
                        entity::setVlt_vol_gk_16_48_ratio),

                timedIfZero("vlt_vol_gk_32_48_ratio", entity::getVlt_vol_gk_32_48_ratio,
                        () -> rangeVolDerivation.gkRatio(series, last, 32, 48),
                        entity::setVlt_vol_gk_32_48_ratio),

                // PARK RATIOS (2)
                timedIfZero("vlt_vol_park_16_48_ratio", entity::getVlt_vol_park_16_48_ratio,
                        () -> rangeVolDerivation.parkRatio(series, last, 16, 48),
                        entity::setVlt_vol_park_16_48_ratio),

                timedIfZero("vlt_vol_park_32_48_ratio", entity::getVlt_vol_park_32_48_ratio,
                        () -> rangeVolDerivation.parkRatio(series, last, 32, 48),
                        entity::setVlt_vol_park_32_48_ratio),

                // RS RATIOS (2)
                timedIfZero("vlt_vol_rs_16_48_ratio", entity::getVlt_vol_rs_16_48_ratio,
                        () -> rangeVolDerivation.rsRatio(series, last, 16, 48),
                        entity::setVlt_vol_rs_16_48_ratio),

                timedIfZero("vlt_vol_rs_32_48_ratio", entity::getVlt_vol_rs_32_48_ratio,
                        () -> rangeVolDerivation.rsRatio(series, last, 32, 48),
                        entity::setVlt_vol_rs_32_48_ratio)
        );

        execute(tasks);
    }

    private Callable<Void> timedIfZero(String name, Supplier<Double> getter, Supplier<Double> calc, Consumer<Double> setter) {
        return () -> {
            Double cur = getter.get();
            if (cur == null || cur == 0) {
                setter.accept(DoubleValidator.validate(calc.get(), name));
            }
            return null;
        };
    }
}
