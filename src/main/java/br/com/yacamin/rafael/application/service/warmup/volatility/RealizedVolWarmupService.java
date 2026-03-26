package br.com.yacamin.rafael.application.service.warmup.volatility;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.RvCacheService;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volatility.RealizedVolDerivation;
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
public class RealizedVolWarmupService {

    private static final int RV_SLP_WINDOW = 20;

    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    private final RvCacheService rvCacheService;
    private final RealizedVolDerivation realizedVolDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VLT][RV] erro interno no calculo", ee.getCause());
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
        var interval = candle.getInterval();

        log.info("[WARMUP][VLT][RV] {} - {}", symbol, openTime);

        var rv10 = rvCacheService.getRv10(symbol, interval, series);
        var rv30 = rvCacheService.getRv30(symbol, interval, series);
        var rv48 = rvCacheService.getRv48(symbol, interval, series);

        List<Callable<Void>> tasks = List.of(

                // RV SLOPES (2)
                timedIfZero("vlt_vol_rv_10_slp", entity::getVlt_vol_rv_10_slp,
                        () -> realizedVolDerivation.slope(rv10, last, RV_SLP_WINDOW),
                        entity::setVlt_vol_rv_10_slp),

                timedIfZero("vlt_vol_rv_30_slp", entity::getVlt_vol_rv_30_slp,
                        () -> realizedVolDerivation.slope(rv30, last, RV_SLP_WINDOW),
                        entity::setVlt_vol_rv_30_slp),

                // RV RATIOS (2)
                timedIfZero("vlt_vol_rv_10_30_ratio", entity::getVlt_vol_rv_10_30_ratio,
                        () -> realizedVolDerivation.ratio(rv10, rv30, last),
                        entity::setVlt_vol_rv_10_30_ratio),

                timedIfZero("vlt_vol_rv_10_48_ratio", entity::getVlt_vol_rv_10_48_ratio,
                        () -> realizedVolDerivation.ratio(rv10, rv48, last),
                        entity::setVlt_vol_rv_10_48_ratio)
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
