package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.trend.CloseCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.AtrNormalizeDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.ClosePriceDerivation;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MomentumIndicatorEntity;
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
public class ClosePriceWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(12);

    private final CloseCacheService closeCache;
    private final AtrCacheService atrCache;
    private final AtrNormalizeDerivation atrNormalize;
    private final ClosePriceDerivation derivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][CLOSE] erro interno no calculo",
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
            MomentumIndicatorEntity entity,
            SymbolCandle candle,
            BarSeries series
    ) {
        var symbol   = candle.getSymbol();
        var interval = candle.getInterval();
        var openTime = candle.getOpenTime();
        int last     = series.getEndIndex();

        log.info("[WARMUP][MOM][CLOSE] {} - {}", symbol, openTime);

        var atr14 = atrCache.getAtr14(symbol, interval, series);

        // SLOPES (cache)
        LinearRegressionSlopeIndicator slp3   = closeCache.getClose3Slp(symbol, interval, series);
        LinearRegressionSlopeIndicator slp8   = closeCache.getClose8Slp(symbol, interval, series);
        LinearRegressionSlopeIndicator slp14  = closeCache.getClose14Slp(symbol, interval, series);

        DifferenceIndicator acc3   = closeCache.getClose3SlpAcc(symbol, interval, series);
        DifferenceIndicator acc8   = closeCache.getClose8SlpAcc(symbol, interval, series);
        DifferenceIndicator acc14  = closeCache.getClose14SlpAcc(symbol, interval, series);

        // Pre-compute (avoid entity dependency in parallel)
        final double slp3v   = derivation.calculateSlope(slp3, last);
        final double slp8v   = derivation.calculateSlope(slp8, last);
        final double slp14v  = derivation.calculateSlope(slp14, last);

        final double acc3v   = derivation.calculateSlopeAcc(acc3, last);
        final double acc8v   = derivation.calculateSlopeAcc(acc8, last);
        final double acc14v  = derivation.calculateSlopeAcc(acc14, last);

        List<Callable<Void>> tasks = List.of(
                // SLOPE RAW
                timedIfZero("mom_close_3_slp",   entity::getMom_close_3_slp,   () -> slp3v,   entity::setMom_close_3_slp),
                timedIfZero("mom_close_8_slp",   entity::getMom_close_8_slp,   () -> slp8v,   entity::setMom_close_8_slp),
                timedIfZero("mom_close_14_slp",  entity::getMom_close_14_slp,  () -> slp14v,  entity::setMom_close_14_slp),

                // SLOPE ATR-N
                timedIfZero("mom_close_3_slp_atrn",   entity::getMom_close_3_slp_atrn,   () -> atrNormalize.normalize(atr14, last, slp3v),   entity::setMom_close_3_slp_atrn),
                timedIfZero("mom_close_8_slp_atrn",   entity::getMom_close_8_slp_atrn,   () -> atrNormalize.normalize(atr14, last, slp8v),   entity::setMom_close_8_slp_atrn),
                timedIfZero("mom_close_14_slp_atrn",  entity::getMom_close_14_slp_atrn,  () -> atrNormalize.normalize(atr14, last, slp14v),  entity::setMom_close_14_slp_atrn),

                // SLOPE ACC
                timedIfZero("mom_close_3_slp_acc",   entity::getMom_close_3_slp_acc,   () -> acc3v,   entity::setMom_close_3_slp_acc),
                timedIfZero("mom_close_8_slp_acc",   entity::getMom_close_8_slp_acc,   () -> acc8v,   entity::setMom_close_8_slp_acc),
                timedIfZero("mom_close_14_slp_acc",  entity::getMom_close_14_slp_acc,  () -> acc14v,  entity::setMom_close_14_slp_acc),

                // SLOPE ACC ATR-N
                timedIfZero("mom_close_3_slp_acc_atrn",   entity::getMom_close_3_slp_acc_atrn,   () -> atrNormalize.normalize(atr14, last, acc3v),   entity::setMom_close_3_slp_acc_atrn),
                timedIfZero("mom_close_8_slp_acc_atrn",   entity::getMom_close_8_slp_acc_atrn,   () -> atrNormalize.normalize(atr14, last, acc8v),   entity::setMom_close_8_slp_acc_atrn),
                timedIfZero("mom_close_14_slp_acc_atrn",  entity::getMom_close_14_slp_acc_atrn,  () -> atrNormalize.normalize(atr14, last, acc14v),  entity::setMom_close_14_slp_acc_atrn)
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
            Double v = getter.get();
            if (v == null || v == 0d) {
                setter.accept(DoubleValidator.validate(calc.get(), name));
            }
            return null;
        };
    }
}
