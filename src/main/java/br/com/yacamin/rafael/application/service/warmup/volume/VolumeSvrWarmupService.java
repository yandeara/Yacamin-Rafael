package br.com.yacamin.rafael.application.service.warmup.volume;

import br.com.yacamin.rafael.application.service.warmup.DoubleValidator;
import br.com.yacamin.rafael.application.service.indicator.derivate.volume.SignedVolumeRatioDerivation;
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
public class VolumeSvrWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    private final SignedVolumeRatioDerivation svrDerivation;

    private void execute(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);
            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException("[WARMUP][VOL][SVR] erro interno no cálculo", ee.getCause());
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
        int end      = series.getEndIndex();

        log.info("[WARMUP][VOL][SVR] {} - {}", symbol, openTime);

        List<Callable<Void>> tasks = List.of(

                timedIfZero("vol_svr",
                        entity::getVol_svr,
                        () -> svrDerivation.svr(candle, series, end),
                        entity::setVol_svr
                ),

                timedIfZero("vol_svr_slp_w20",
                        entity::getVol_svr_slp_w20,
                        () -> svrDerivation.svrSlopeW20(series, end),
                        entity::setVol_svr_slp_w20
                ),

                timedIfZero("vol_svr_acc_5",
                        entity::getVol_svr_acc_5,
                        () -> svrDerivation.svrAcc(candle, series, end, 5),
                        entity::setVol_svr_acc_5
                ),

                timedIfZero("vol_svr_acc_10",
                        entity::getVol_svr_acc_10,
                        () -> svrDerivation.svrAcc(candle, series, end, 10),
                        entity::setVol_svr_acc_10
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
