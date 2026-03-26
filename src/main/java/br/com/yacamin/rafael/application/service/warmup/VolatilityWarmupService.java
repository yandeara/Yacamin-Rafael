package br.com.yacamin.rafael.application.service.warmup;

import br.com.yacamin.rafael.application.service.warmup.volatility.*;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.VolatilityIndicatorEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.VolatilityIndicator15MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.VolatilityIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.VolatilityIndicator5MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.VolatilityIndicator1MnEntity;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn.VolatilityIndicator15MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn.VolatilityIndicator30MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.VolatilityIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.VolatilityIndicator1MnRepository;
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
public class VolatilityWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(7);

    private final VolatilityIndicator15MnRepository i15Repository;
    private final VolatilityIndicator30MnRepository i30Repository;
    private final VolatilityIndicator1MnRepository i1Repository;
    private final VolatilityIndicator5MnRepository i5Repository;

    private final AtrWarmupService atrWarmupService;
    private final BollingerWarmupService bollingerWarmupService;
    private final EwmaVolWarmupService ewmaVolWarmupService;
    private final KeltnerWarmupService keltnerWarmupService;
    private final RangeVolWarmupService rangeVolWarmupService;
    private final RealizedVolWarmupService realizedVolWarmupService;
    private final StdWarmupService stdWarmupService;

    public void analyse(SymbolCandle candle, BarSeries series) {

        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();

        log.info("[WARMUP][VLT] {} - {}", symbol, openTime);

        VolatilityIndicatorEntity entity;

        switch (candle.getInterval()) {
            case I1_MN -> entity = i1Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewOneMn(candle));
            case I15_MN -> entity = i15Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewFifteenMn(candle));
            case I5_MN -> entity = i5Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew5m(candle));
            case I30_MN -> entity = i30Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew30m(candle));
            default -> throw new RuntimeException();
        }

        List<Callable<Void>> tasks = List.of(
                () -> { atrWarmupService.analyse(entity, candle, series); return null; },
                () -> { bollingerWarmupService.analyse(entity, candle, series); return null; },
                () -> { ewmaVolWarmupService.analyse(entity, candle, series); return null; },
                () -> { keltnerWarmupService.analyse(entity, candle, series); return null; },
                () -> { rangeVolWarmupService.analyse(entity, candle, series); return null; },
                () -> { realizedVolWarmupService.analyse(entity, candle, series); return null; },
                () -> { stdWarmupService.analyse(entity, candle, series); return null; }
        );

        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    throw new RuntimeException(
                            "[WARMUP] erro interno no calculo",
                            ee.getCause()
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        switch (candle.getInterval()) {
            case I1_MN -> i1Repository.save((VolatilityIndicator1MnEntity) entity);
            case I15_MN -> i15Repository.save((VolatilityIndicator15MnEntity) entity);
            case I5_MN ->  i5Repository.save((VolatilityIndicator5MnEntity) entity);
            case I30_MN -> i30Repository.save((VolatilityIndicator30MnEntity) entity);
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
                            "[WARMUP][TRD] erro interno no calculo",
                            ee.getCause()
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private VolatilityIndicator1MnEntity createNewOneMn(SymbolCandle candle) {
        var e = new VolatilityIndicator1MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private VolatilityIndicator15MnEntity createNewFifteenMn(SymbolCandle candle) {
        var e = new VolatilityIndicator15MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private VolatilityIndicator30MnEntity createNew30m(SymbolCandle candle) {
        var e = new VolatilityIndicator30MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private VolatilityIndicator5MnEntity createNew5m(SymbolCandle candle) {
        var e = new VolatilityIndicator5MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }
}
