package br.com.yacamin.rafael.application.service.warmup.momentum;

import br.com.yacamin.rafael.application.service.cache.indicator.momentum.RsiCacheService;
import br.com.yacamin.rafael.application.service.cache.indicator.volatility.AtrCacheService;
import br.com.yacamin.rafael.application.service.indicator.derivate.helper.AtrNormalizeDerivation;
import br.com.yacamin.rafael.application.service.indicator.derivate.momentum.RsiDerivation;
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
public class RsiWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private final RsiDerivation rsiDerivation;
    private final RsiCacheService rsiCache;
    private final AtrNormalizeDerivation atrNormalizeDerivation;
    private final AtrCacheService atrCacheService;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][MOM][RSI] erro interno no calculo",
                            ee.getCause()
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void analyse(MomentumIndicatorEntity entity,
                        SymbolCandle candle,
                        BarSeries series) {

        var symbol   = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var index    = series.getEndIndex();
        var interval = candle.getInterval();

        log.info("[WARMUP][MOM][RSI] {} - {}", symbol, openTime);

        var rsi2   = rsiCache.getRsi2(symbol, interval, series);
        var rsi3   = rsiCache.getRsi3(symbol, interval, series);
        var rsi5   = rsiCache.getRsi5(symbol, interval, series);
        var rsi7   = rsiCache.getRsi7(symbol, interval, series);
        var rsi14  = rsiCache.getRsi14(symbol, interval, series);

        var rsi7slp  = rsiCache.getRsi7Slp(symbol, interval, series);
        var rsi14slp = rsiCache.getRsi14Slp(symbol, interval, series);

        var atr14 = atrCacheService.getAtr14(symbol, interval, series);

        List<Callable<Void>> derivate = List.of(
                // RSI DELTA
                timedIfZero("mom_rsi_2_dlt",  entity::getMom_rsi_2_dlt,  () -> rsiDerivation.calculateDelta(rsi2, index),  entity::setMom_rsi_2_dlt),
                timedIfZero("mom_rsi_3_dlt",  entity::getMom_rsi_3_dlt,  () -> rsiDerivation.calculateDelta(rsi3, index),  entity::setMom_rsi_3_dlt),
                timedIfZero("mom_rsi_5_dlt",  entity::getMom_rsi_5_dlt,  () -> rsiDerivation.calculateDelta(rsi5, index),  entity::setMom_rsi_5_dlt),
                timedIfZero("mom_rsi_7_dlt",  entity::getMom_rsi_7_dlt,  () -> rsiDerivation.calculateDelta(rsi7, index),  entity::setMom_rsi_7_dlt),
                timedIfZero("mom_rsi_14_dlt", entity::getMom_rsi_14_dlt, () -> rsiDerivation.calculateDelta(rsi14, index), entity::setMom_rsi_14_dlt),

                // RSI ROC
                timedIfZero("mom_rsi_2_roc",  entity::getMom_rsi_2_roc,  () -> rsiDerivation.calculateRoc(rsi2, index, 1),  entity::setMom_rsi_2_roc),
                timedIfZero("mom_rsi_3_roc",  entity::getMom_rsi_3_roc,  () -> rsiDerivation.calculateRoc(rsi3, index, 1),  entity::setMom_rsi_3_roc),
                timedIfZero("mom_rsi_5_roc",  entity::getMom_rsi_5_roc,  () -> rsiDerivation.calculateRoc(rsi5, index, 2),  entity::setMom_rsi_5_roc),
                timedIfZero("mom_rsi_7_roc",  entity::getMom_rsi_7_roc,  () -> rsiDerivation.calculateRoc(rsi7, index, 2),  entity::setMom_rsi_7_roc),
                timedIfZero("mom_rsi_14_roc", entity::getMom_rsi_14_roc, () -> rsiDerivation.calculateRoc(rsi14, index, 3), entity::setMom_rsi_14_roc),

                // RSI SLOPE
                timedIfZero("mom_rsi_7_slp",  entity::getMom_rsi_7_slp,  () -> rsi7slp.getValue(index).doubleValue(),  entity::setMom_rsi_7_slp),
                timedIfZero("mom_rsi_14_slp", entity::getMom_rsi_14_slp, () -> rsi14slp.getValue(index).doubleValue(), entity::setMom_rsi_14_slp),

                // RSI 14 ACC
                timedIfZero("mom_rsi_14_acc", entity::getMom_rsi_14_acc, () -> rsiDerivation.calculateAcceleration(rsi14, index, 2), entity::setMom_rsi_14_acc),

                // RSI 14 ATRN
                timedIfZero("mom_rsi_14_atrn", entity::getMom_rsi_14_atrn,
                        () -> atrNormalizeDerivation.normalize(atr14, index, rsi14.getValue(index).doubleValue()),
                        entity::setMom_rsi_14_atrn),

                // DIST MID
                timedIfZero("mom_rsi_7_dst_mid",  entity::getMom_rsi_7_dst_mid,  () -> rsiDerivation.calculateDistMid(rsi7, index),  entity::setMom_rsi_7_dst_mid),
                timedIfZero("mom_rsi_14_dst_mid", entity::getMom_rsi_14_dst_mid, () -> rsiDerivation.calculateDistMid(rsi14, index), entity::setMom_rsi_14_dst_mid),

                // TAIL
                timedIfZero("mom_rsi_7_tail_up",  entity::getMom_rsi_7_tail_up,  () -> rsiDerivation.calculateTailUp(rsi7, index, 70.0),  entity::setMom_rsi_7_tail_up),
                timedIfZero("mom_rsi_7_tail_dw",  entity::getMom_rsi_7_tail_dw,  () -> rsiDerivation.calculateTailDown(rsi7, index, 30.0), entity::setMom_rsi_7_tail_dw),
                timedIfZero("mom_rsi_14_tail_up", entity::getMom_rsi_14_tail_up, () -> rsiDerivation.calculateTailUp(rsi14, index, 70.0), entity::setMom_rsi_14_tail_up),
                timedIfZero("mom_rsi_14_tail_dw", entity::getMom_rsi_14_tail_dw, () -> rsiDerivation.calculateTailDown(rsi14, index, 30.0), entity::setMom_rsi_14_tail_dw)
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
