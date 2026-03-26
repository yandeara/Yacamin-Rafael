package br.com.yacamin.rafael.application.service.warmup.volatility;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volatility.AtrDerivation;
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
public class AtrWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    private final AtrCacheService atrCacheService;
    private final AtrDerivation atrDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VLT][ATR] erro interno no calculo", ee.getCause());
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

        log.info("[WARMUP][VLT][ATR] {} - {}", symbol, openTime);

        var atr7  = atrCacheService.getAtr7(symbol, interval, series);
        var atr14 = atrCacheService.getAtr14(symbol, interval, series);
        var atr21 = atrCacheService.getAtr21(symbol, interval, series);

        var atr7Slp  = atrCacheService.getAtr7Slp(symbol, interval, series);
        var atr14Slp = atrCacheService.getAtr14Slp(symbol, interval, series);
        var atr21Slp = atrCacheService.getAtr21Slp(symbol, interval, series);

        List<Callable<Void>> tasks = List.of(

                // ATR CHANGE (3)
                timedIfZero("vlt_atr_7_chg",  entity::getVlt_atr_7_chg,  () -> atrDerivation.atrChange(atr7,  last), entity::setVlt_atr_7_chg),
                timedIfZero("vlt_atr_14_chg", entity::getVlt_atr_14_chg, () -> atrDerivation.atrChange(atr14, last), entity::setVlt_atr_14_chg),
                timedIfZero("vlt_atr_21_chg", entity::getVlt_atr_21_chg, () -> atrDerivation.atrChange(atr21, last), entity::setVlt_atr_21_chg),

                // ATR LOCAL (2)
                timedIfZero("vlt_range_atr_14_loc", entity::getVlt_range_atr_14_loc,
                        () -> atrDerivation.rangeAtrLocal(series, atr14, last),
                        entity::setVlt_range_atr_14_loc),

                timedIfZero("vlt_range_atr_14_loc_chg", entity::getVlt_range_atr_14_loc_chg,
                        () -> atrDerivation.rangeAtrLocalChange(series, atr14, last),
                        entity::setVlt_range_atr_14_loc_chg),

                // ATR REGIME 7/21 (3)
                timedIfZero("vlt_atr_7_21_ratio", entity::getVlt_atr_7_21_ratio,
                        () -> atrDerivation.atrRatio(atr7, atr21, last),
                        entity::setVlt_atr_7_21_ratio),

                timedIfZero("vlt_atr_7_21_expn", entity::getVlt_atr_7_21_expn,
                        () -> atrDerivation.expansionFromRatio(atrDerivation.atrRatio(atr7, atr21, last)),
                        entity::setVlt_atr_7_21_expn),

                timedIfZero("vlt_atr_7_21_cmpr", entity::getVlt_atr_7_21_cmpr,
                        () -> atrDerivation.compressionFromRatio(atrDerivation.atrRatio(atr7, atr21, last)),
                        entity::setVlt_atr_7_21_cmpr),

                // ATR SLOPES (3)
                timedIfZero("vlt_atr_7_slp",  entity::getVlt_atr_7_slp,  () -> atrDerivation.atrSlope(atr7Slp,  last), entity::setVlt_atr_7_slp),
                timedIfZero("vlt_atr_14_slp", entity::getVlt_atr_14_slp, () -> atrDerivation.atrSlope(atr14Slp, last), entity::setVlt_atr_14_slp),
                timedIfZero("vlt_atr_21_slp", entity::getVlt_atr_21_slp, () -> atrDerivation.atrSlope(atr21Slp, last), entity::setVlt_atr_21_slp)
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
