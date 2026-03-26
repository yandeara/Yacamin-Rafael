package br.com.yacamin.rafael.application.service.warmup.volatility;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volatility.EwmaVolDerivation;
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
public class EwmaVolWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    private final EwmaVolDerivation ewmaVolDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VLT][EWMA] erro interno no calculo", ee.getCause());
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

        log.info("[WARMUP][VLT][EWMA] {} - {}", symbol, openTime);

        final int EWMA_SLP_WINDOW = 20;

        List<Callable<Void>> tasks = List.of(

                // EWMA SLOPES (2)
                timedIfZero("vlt_ewma_vol_20_slp", entity::getVlt_ewma_vol_20_slp,
                        () -> ewmaVolDerivation.ewmaVolSlope(series, last, 20, EWMA_SLP_WINDOW),
                        entity::setVlt_ewma_vol_20_slp),

                timedIfZero("vlt_ewma_vol_32_slp", entity::getVlt_ewma_vol_32_slp,
                        () -> ewmaVolDerivation.ewmaVolSlope(series, last, 32, EWMA_SLP_WINDOW),
                        entity::setVlt_ewma_vol_32_slp),

                // EWMA RATIOS (2)
                timedIfZero("vlt_ewma_vol_20_48_ratio", entity::getVlt_ewma_vol_20_48_ratio,
                        () -> ewmaVolDerivation.ewmaVolRatio(series, last, 20, 48),
                        entity::setVlt_ewma_vol_20_48_ratio),

                timedIfZero("vlt_ewma_vol_32_48_ratio", entity::getVlt_ewma_vol_32_48_ratio,
                        () -> ewmaVolDerivation.ewmaVolRatio(series, last, 32, 48),
                        entity::setVlt_ewma_vol_32_48_ratio)
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
