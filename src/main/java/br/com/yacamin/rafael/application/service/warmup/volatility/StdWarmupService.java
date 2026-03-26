package br.com.yacamin.rafael.application.service.warmup.volatility;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.StdCacheService;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volatility.StdDerivation;
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
public class StdWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    private final StdCacheService stdCacheService;
    private final StdDerivation stdDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VLT][STD] erro interno no calculo", ee.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void analyse(VolatilityIndicatorEntity entity, SymbolCandle candle, BarSeries series) {

        var symbol   = candle.getSymbol();
        var interval = candle.getInterval();
        var openTime = candle.getOpenTime();
        int last     = series.getEndIndex();

        log.info("[WARMUP][VLT][STD] {} - {}", symbol, openTime);

        var std14 = stdCacheService.getStd14(symbol, interval, series);
        var std20 = stdCacheService.getStd20(symbol, interval, series);
        var std50 = stdCacheService.getStd50(symbol, interval, series);
        var std48 = stdCacheService.getStd48(symbol, interval, series);

        var std14Slp = stdCacheService.getStd14Slp(symbol, interval, series);
        var std20Slp = stdCacheService.getStd20Slp(symbol, interval, series);
        var std50Slp = stdCacheService.getStd50Slp(symbol, interval, series);

        List<Callable<Void>> tasks = List.of(

                // STD CHANGE (3)
                timedIfZero("vlt_std_14_chg", entity::getVlt_std_14_chg, () -> stdDerivation.stdChange(std14, last), entity::setVlt_std_14_chg),
                timedIfZero("vlt_std_20_chg", entity::getVlt_std_20_chg, () -> stdDerivation.stdChange(std20, last), entity::setVlt_std_20_chg),
                timedIfZero("vlt_std_50_chg", entity::getVlt_std_50_chg, () -> stdDerivation.stdChange(std50, last), entity::setVlt_std_50_chg),

                // STD REGIME 14/50 (3)
                timedIfZero("vlt_std_14_50_ratio", entity::getVlt_std_14_50_ratio,
                        () -> stdDerivation.stdRatio(std14, std50, last),
                        entity::setVlt_std_14_50_ratio),

                timedIfZero("vlt_std_14_50_expn", entity::getVlt_std_14_50_expn,
                        () -> stdDerivation.expansionFromRatio(stdDerivation.stdRatio(std14, std50, last)),
                        entity::setVlt_std_14_50_expn),

                timedIfZero("vlt_std_14_50_cmpr", entity::getVlt_std_14_50_cmpr,
                        () -> stdDerivation.compressionFromRatio(stdDerivation.stdRatio(std14, std50, last)),
                        entity::setVlt_std_14_50_cmpr),

                // STD REGIME 14/48 (3)
                timedIfZero("vlt_std_14_48_ratio", entity::getVlt_std_14_48_ratio,
                        () -> stdDerivation.stdRatio(std14, std48, last),
                        entity::setVlt_std_14_48_ratio),

                timedIfZero("vlt_std_14_48_expn", entity::getVlt_std_14_48_expn,
                        () -> stdDerivation.expansionFromRatio(stdDerivation.stdRatio(std14, std48, last)),
                        entity::setVlt_std_14_48_expn),

                timedIfZero("vlt_std_14_48_cmpr", entity::getVlt_std_14_48_cmpr,
                        () -> stdDerivation.compressionFromRatio(stdDerivation.stdRatio(std14, std48, last)),
                        entity::setVlt_std_14_48_cmpr),

                // STD SLOPES (3)
                timedIfZero("vlt_std_14_slp", entity::getVlt_std_14_slp, () -> stdDerivation.stdSlope(std14Slp, last), entity::setVlt_std_14_slp),
                timedIfZero("vlt_std_20_slp", entity::getVlt_std_20_slp, () -> stdDerivation.stdSlope(std20Slp, last), entity::setVlt_std_20_slp),
                timedIfZero("vlt_std_50_slp", entity::getVlt_std_50_slp, () -> stdDerivation.stdSlope(std50Slp, last), entity::setVlt_std_50_slp)
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
