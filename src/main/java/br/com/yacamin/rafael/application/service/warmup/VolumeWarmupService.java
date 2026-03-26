package br.com.yacamin.rafael.application.service.warmup;

import br.com.yacamin.rafael.application.service.warmup.volume.*;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.VolumeIndicatorEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.VolumeIndicator15MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.VolumeIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.VolumeIndicator5MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.VolumeIndicator1MnEntity;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn.VolumeIndicator15MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn.VolumeIndicator30MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.VolumeIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.VolumeIndicator1MnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolumeWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(9);


    private final VolumeIndicator15MnRepository i15Repository;
    private final VolumeIndicator30MnRepository i30Repository;
    private final VolumeIndicator1MnRepository i1Repository;
    private final VolumeIndicator5MnRepository i5Repository;


    private final VolumeActivityPressureWarmupService volumeActivityPressureWarmupService;
    private final VolumeBapWarmupService volumeBapWarmupService;
    private final VolumeDeltaWarmupService volumeDeltaWarmupService;
    private final VolumeMicroburstWarmupService volumeMicroburstWarmupService;
    private final VolumeOfiWarmupService volumeOfiWarmupService;
    private final VolumeRawMicrostructureWarmupService volumeRawMicrostructureWarmupService;
    private final VolumeSlopeWarmupService volumeSlopeWarmupService;
    private final VolumeSvrWarmupService volumeSvrWarmupService;
    private final VolumeVwapWarmupService volumeVwapWarmupService;

    public void analyse(SymbolCandle candle, BarSeries series) {

        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();

        log.info("[WARMUP][VOL] {} - {}", symbol, openTime);

        VolumeIndicatorEntity entity;

        switch (candle.getInterval()) {
            case I1_MN -> entity = i1Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewOneMn(candle));
            case I15_MN -> entity = i15Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewFifteenMn(candle));
            case I5_MN -> entity = i5Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew5m(candle));
            case I30_MN -> entity = i30Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew30m(candle));
            default -> throw new RuntimeException();
        }


        List<Callable<Void>> tasks = List.of(
                () -> { volumeActivityPressureWarmupService.analyse(entity, candle, series); return null; },
                () -> { volumeBapWarmupService.analyse(entity, candle, series); return null; },
                () -> { volumeDeltaWarmupService.analyse(entity, candle, series); return null; },
                () -> { volumeMicroburstWarmupService.analyse(entity, candle, series); return null; },
                () -> { volumeOfiWarmupService.analyse(entity, candle, series); return null; },
                () -> { volumeRawMicrostructureWarmupService.analyse(entity, candle, series); return null; },
                () -> { volumeSlopeWarmupService.analyse(entity, candle, series); return null; },
                () -> { volumeSvrWarmupService.analyse(entity, candle, series); return null; },
                () -> { volumeVwapWarmupService.analyse(entity, candle, series); return null; }
        );

        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP] erro interno no cálculo",
                            ee.getCause()
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        switch (candle.getInterval()) {
            case I1_MN -> i1Repository.save((VolumeIndicator1MnEntity) entity);
            case I15_MN -> i15Repository.save((VolumeIndicator15MnEntity) entity);
            case I5_MN ->  i5Repository.save((VolumeIndicator5MnEntity) entity);
            case I30_MN -> i30Repository.save((VolumeIndicator30MnEntity) entity);
            default -> throw new RuntimeException();
        }
    }

    public void run(List<Callable<Void>> tasks) {
        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP][TRD] erro interno no cálculo",
                            ee.getCause()
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }


    private VolumeIndicator1MnEntity createNewOneMn(SymbolCandle candle) {
        var e = new VolumeIndicator1MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private VolumeIndicator15MnEntity createNewFifteenMn(SymbolCandle candle) {
        var e = new VolumeIndicator15MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private VolumeIndicator30MnEntity createNew30m(SymbolCandle candle) {
        var e = new VolumeIndicator30MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private VolumeIndicator5MnEntity createNew5m(SymbolCandle candle) {
        var e = new VolumeIndicator5MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

}
