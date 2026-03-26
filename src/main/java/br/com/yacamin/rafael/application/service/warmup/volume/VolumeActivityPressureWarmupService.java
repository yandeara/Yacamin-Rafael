package br.com.yacamin.rafael.application.service.warmup.volume;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volume.PressureDerivation;
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
public class VolumeActivityPressureWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(8);

    private final PressureDerivation pressure;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VOL][ACT] erro interno no cálculo", ee.getCause());
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

        var symbol   = candle.getSymbol();
        var openTime = candle.getOpenTime();

        log.info("[WARMUP][VOL][ACT] {} - {}", symbol, openTime);

        List<Callable<Void>> tasks = List.of(

                // TRADES sp/acc 16/32
                timedIfZero("vol_act_trades_sp_16",
                        entity::getVol_act_trades_sp_16,
                        () -> pressure.sustainedPressure(series, 16, PressureDerivation.TRADES),
                        entity::setVol_act_trades_sp_16
                ),

                timedIfZero("vol_act_trades_sp_32",
                        entity::getVol_act_trades_sp_32,
                        () -> pressure.sustainedPressure(series, 32, PressureDerivation.TRADES),
                        entity::setVol_act_trades_sp_32
                ),

                timedIfZero("vol_act_trades_acc_16",
                        entity::getVol_act_trades_acc_16,
                        () -> pressure.pressureAcc(series, 16, PressureDerivation.TRADES),
                        entity::setVol_act_trades_acc_16
                ),

                timedIfZero("vol_act_trades_acc_32",
                        entity::getVol_act_trades_acc_32,
                        () -> pressure.pressureAcc(series, 32, PressureDerivation.TRADES),
                        entity::setVol_act_trades_acc_32
                ),

                // QUOTE sp/acc 16/32
                timedIfZero("vol_act_quote_sp_16",
                        entity::getVol_act_quote_sp_16,
                        () -> pressure.sustainedPressure(series, 16, PressureDerivation.QUOTE),
                        entity::setVol_act_quote_sp_16
                ),

                timedIfZero("vol_act_quote_sp_32",
                        entity::getVol_act_quote_sp_32,
                        () -> pressure.sustainedPressure(series, 32, PressureDerivation.QUOTE),
                        entity::setVol_act_quote_sp_32
                ),

                timedIfZero("vol_act_quote_acc_16",
                        entity::getVol_act_quote_acc_16,
                        () -> pressure.pressureAcc(series, 16, PressureDerivation.QUOTE),
                        entity::setVol_act_quote_acc_16
                ),

                timedIfZero("vol_act_quote_acc_32",
                        entity::getVol_act_quote_acc_32,
                        () -> pressure.pressureAcc(series, 32, PressureDerivation.QUOTE),
                        entity::setVol_act_quote_acc_32
                )
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
            Double cur = getter.get();
            if (cur == null || cur == 0) {
                setter.accept(DoubleValidator.validate(calc.get(), name));
            }
            return null;
        };
    }
}
