package br.com.yacamin.rafael.application.service.warmup.volatility;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.BollingerCacheService;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volatility.KeltnerDerivation;
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
public class KeltnerWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(1);

    private final BollingerCacheService bollingerCacheService;
    private final AtrCacheService atrCacheService;
    private final KeltnerDerivation keltnerDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VLT][KELT] erro interno no calculo", ee.getCause());
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

        log.info("[WARMUP][VLT][KELT] {} - {}", symbol, openTime);

        var bb20  = bollingerCacheService.getBb20(symbol, interval, series);
        var atr14 = atrCacheService.getAtr14(symbol, interval, series);

        List<Callable<Void>> tasks = List.of(

                // vlt_vol_sqz_bb_kelt_chg (legacy squeeze change)
                timedIfZero("vlt_vol_sqz_bb_kelt_chg",
                        entity::getVlt_vol_sqz_bb_kelt_chg,
                        () -> keltnerDerivation.squeezeChange(bb20, atr14, last),
                        entity::setVlt_vol_sqz_bb_kelt_chg),

                // vlt_vol_sqz_bb_kelt_20_chg (squeeze 20 change)
                timedIfZero("vlt_vol_sqz_bb_kelt_20_chg",
                        entity::getVlt_vol_sqz_bb_kelt_20_chg,
                        () -> keltnerDerivation.squeezeChange(bb20, atr14, last),
                        entity::setVlt_vol_sqz_bb_kelt_20_chg)
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
