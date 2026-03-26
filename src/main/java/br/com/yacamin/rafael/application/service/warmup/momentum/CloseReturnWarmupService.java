package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.AtrNormalizeDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.CloseReturnDerivation;
import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MomentumIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloseReturnWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(16);

    private final CloseReturnDerivation closeReturnDerivation;

    private final AtrCacheService atrCacheService;
    private final AtrNormalizeDerivation atrNormalizeDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][CLOSE-RETURN] erro interno no calculo",
                            ee.getCause()
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void analyse(MomentumIndicatorEntity entity, SymbolCandle candle, BarSeries series) {

        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var index = series.getEndIndex();

        var atr14 = atrCacheService.getAtr14(symbol, candle.getInterval(), series);

        log.info("[WARMUP][MOM][CLOSE-RETURN] {} - {}", symbol, openTime);

        // ==========================================================================
        // PRIORITY: RAW CLOSE RETURNS (intermediaries for _atrn)
        // ==========================================================================
        List<Callable<Void>> priority = List.of(
                timedIfZero("mom_close_ret_1",  entity::getMom_close_ret_1,  () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 1),  entity::setMom_close_ret_1),
                timedIfZero("mom_close_ret_2",  entity::getMom_close_ret_2,  () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 2),  entity::setMom_close_ret_2),
                timedIfZero("mom_close_ret_3",  entity::getMom_close_ret_3,  () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 3),  entity::setMom_close_ret_3),
                timedIfZero("mom_close_ret_4",  entity::getMom_close_ret_4,  () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 4),  entity::setMom_close_ret_4),
                timedIfZero("mom_close_ret_5",  entity::getMom_close_ret_5,  () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 5),  entity::setMom_close_ret_5),
                timedIfZero("mom_close_ret_6",  entity::getMom_close_ret_6,  () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 6),  entity::setMom_close_ret_6),
                timedIfZero("mom_close_ret_8",  entity::getMom_close_ret_8,  () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 8),  entity::setMom_close_ret_8),
                timedIfZero("mom_close_ret_10", entity::getMom_close_ret_10, () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 10), entity::setMom_close_ret_10),
                timedIfZero("mom_close_ret_12", entity::getMom_close_ret_12, () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 12), entity::setMom_close_ret_12),
                timedIfZero("mom_close_ret_16", entity::getMom_close_ret_16, () -> closeReturnDerivation.calculateSimpleReturn(candle, series, index, 16), entity::setMom_close_ret_16)
        );

        execute(priority);

        // ==========================================================================
        // DERIVATE: _atrn + burst + cntrate + decay + impls + chprt
        // ==========================================================================
        List<Callable<Void>> derivate = List.of(
                // CLOSE RETURNS — ATR-N
                timedIfZero("mom_close_ret_1_atrn",  entity::getMom_close_ret_1_atrn,  () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_1()),  entity::setMom_close_ret_1_atrn),
                timedIfZero("mom_close_ret_2_atrn",  entity::getMom_close_ret_2_atrn,  () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_2()),  entity::setMom_close_ret_2_atrn),
                timedIfZero("mom_close_ret_3_atrn",  entity::getMom_close_ret_3_atrn,  () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_3()),  entity::setMom_close_ret_3_atrn),
                timedIfZero("mom_close_ret_4_atrn",  entity::getMom_close_ret_4_atrn,  () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_4()),  entity::setMom_close_ret_4_atrn),
                timedIfZero("mom_close_ret_5_atrn",  entity::getMom_close_ret_5_atrn,  () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_5()),  entity::setMom_close_ret_5_atrn),
                timedIfZero("mom_close_ret_6_atrn",  entity::getMom_close_ret_6_atrn,  () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_6()),  entity::setMom_close_ret_6_atrn),
                timedIfZero("mom_close_ret_8_atrn",  entity::getMom_close_ret_8_atrn,  () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_8()),  entity::setMom_close_ret_8_atrn),
                timedIfZero("mom_close_ret_10_atrn", entity::getMom_close_ret_10_atrn, () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_10()), entity::setMom_close_ret_10_atrn),
                timedIfZero("mom_close_ret_12_atrn", entity::getMom_close_ret_12_atrn, () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_12()), entity::setMom_close_ret_12_atrn),
                timedIfZero("mom_close_ret_16_atrn", entity::getMom_close_ret_16_atrn, () -> atrNormalizeDerivation.normalize(atr14, index, entity.getMom_close_ret_16()), entity::setMom_close_ret_16_atrn),

                // BURST STRENGTH
                timedIfZero("mom_burst_10", entity::getMom_burst_10, () -> closeReturnDerivation.calculateBurstStrength(candle, series, index, 10), entity::setMom_burst_10),
                timedIfZero("mom_burst_16", entity::getMom_burst_16, () -> closeReturnDerivation.calculateBurstStrength(candle, series, index, 16), entity::setMom_burst_16),
                timedIfZero("mom_burst_32", entity::getMom_burst_32, () -> closeReturnDerivation.calculateBurstStrength(candle, series, index, 32), entity::setMom_burst_32),

                // CONTINUATION RATE
                timedIfZero("mom_cntrate_10", entity::getMom_cntrate_10, () -> closeReturnDerivation.calculateContinuationRate(candle, series, index, 10), entity::setMom_cntrate_10),
                timedIfZero("mom_cntrate_16", entity::getMom_cntrate_16, () -> closeReturnDerivation.calculateContinuationRate(candle, series, index, 16), entity::setMom_cntrate_16),
                timedIfZero("mom_cntrate_32", entity::getMom_cntrate_32, () -> closeReturnDerivation.calculateContinuationRate(candle, series, index, 32), entity::setMom_cntrate_32),

                // DECAY RATE
                timedIfZero("mom_decay_10", entity::getMom_decay_10, () -> closeReturnDerivation.calculateDecayRate(candle, series, index, 10), entity::setMom_decay_10),
                timedIfZero("mom_decay_16", entity::getMom_decay_16, () -> closeReturnDerivation.calculateDecayRate(candle, series, index, 16), entity::setMom_decay_16),
                timedIfZero("mom_decay_32", entity::getMom_decay_32, () -> closeReturnDerivation.calculateDecayRate(candle, series, index, 32), entity::setMom_decay_32),

                // IMPULSE
                timedIfZero("mom_impls_10", entity::getMom_impls_10, () -> closeReturnDerivation.calculateImpulse(candle, series, index, 10), entity::setMom_impls_10),
                timedIfZero("mom_impls_16", entity::getMom_impls_16, () -> closeReturnDerivation.calculateImpulse(candle, series, index, 16), entity::setMom_impls_16),
                timedIfZero("mom_impls_32", entity::getMom_impls_32, () -> closeReturnDerivation.calculateImpulse(candle, series, index, 32), entity::setMom_impls_32),

                // CHOP RATIO
                timedIfZero("mom_chprt_10", entity::getMom_chprt_10, () -> closeReturnDerivation.calculateChopRatio(candle, series, index, 10), entity::setMom_chprt_10),
                timedIfZero("mom_chprt_16", entity::getMom_chprt_16, () -> closeReturnDerivation.calculateChopRatio(candle, series, index, 16), entity::setMom_chprt_16),
                timedIfZero("mom_chprt_32", entity::getMom_chprt_32, () -> closeReturnDerivation.calculateChopRatio(candle, series, index, 32), entity::setMom_chprt_32)
        );

        execute(derivate);
    }

    private Callable<Void> timed(String name, Runnable task) {
        return () -> {
            try {
                task.run();
                return null;
            } finally {
                // noop
            }
        };
    }

    private Callable<Void> timedIfZero(
            String name,
            Supplier<Double> getter,
            Supplier<Double> calculator,
            Consumer<Double> setter
    ) {
        return timed(name, () -> {
            Double current = getter.get();
            if (current == null || current == 0) {
                setter.accept(DoubleValidator.validate(calculator.get(), name));
            }
        });
    }
}
