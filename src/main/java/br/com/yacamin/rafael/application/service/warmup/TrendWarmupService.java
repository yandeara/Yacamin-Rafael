package br.com.yacamin.rafael.application.service.warmup;

import br.com.yacamin.rafael.application.service.warmup.trend.AdxWarmupService;
import br.com.yacamin.rafael.application.service.warmup.trend.EmaWarmupService;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.TrendIndicatorEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.TrendIndicator15MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.TrendIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.TrendIndicator5MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.TrendIndicator1MnEntity;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn.TrendIndicator15MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn.TrendIndicator30MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.TrendIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.TrendIndicator1MnRepository;
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
public class TrendWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(32);

    private final EmaWarmupService emaWarmupService;
    private final AdxWarmupService adxWarmupService;

    private final TrendIndicator15MnRepository i15Repository;
    private final TrendIndicator30MnRepository i30Repository;
    private final TrendIndicator1MnRepository i1Repository;
    private final TrendIndicator5MnRepository i5Repository;

    public void analyse(SymbolCandle candle, BarSeries series) {

        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();

        log.info("[WARMUP][TRD] {} - {}", symbol, openTime);

        TrendIndicatorEntity entity;

        switch (candle.getInterval()) {
            case I1_MN -> entity = i1Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewOneMn(candle));
            case I15_MN -> entity = i15Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewFifteenMn(candle));
            case I5_MN -> entity = i5Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew5m(candle));
            case I30_MN -> entity = i30Repository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew30m(candle));
            default -> throw new RuntimeException();
        }

        List<Callable<Void>> tasks = List.of(
                () -> { emaWarmupService.analyse(entity, candle, series); return null; },
                () -> { adxWarmupService.analyse(entity, candle, series); return null; }
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
            case I1_MN -> i1Repository.save((TrendIndicator1MnEntity) entity);
            case I15_MN -> i15Repository.save((TrendIndicator15MnEntity) entity);
            case I5_MN ->  i5Repository.save((TrendIndicator5MnEntity) entity);
            case I30_MN -> i30Repository.save((TrendIndicator30MnEntity) entity);
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

    private TrendIndicator1MnEntity createNewOneMn(SymbolCandle candle) {
        var e = new TrendIndicator1MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private TrendIndicator15MnEntity createNewFifteenMn(SymbolCandle candle) {
        var e = new TrendIndicator15MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private TrendIndicator30MnEntity createNew30m(SymbolCandle candle) {
        var e = new TrendIndicator30MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private TrendIndicator5MnEntity createNew5m(SymbolCandle candle) {
        var e = new TrendIndicator5MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }


}
