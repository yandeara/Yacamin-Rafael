package br.com.yacamin.rafael.application.service.warmup;

import br.com.yacamin.rafael.application.service.warmup.microstructure.*;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.MicrostructureIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.MicrostructureIndicator5MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MicrostructureIndicatorEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.MicrostructureIndicator15MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.MicrostructureIndicator1MnEntity;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn.MicrostructureIndicator15MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn.MicrostructureIndicator30MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.MicrostructureIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.MicrostructureIndicator1MnRepository;
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
public class MicrostructureWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(32);

    private final MicrostructureIndicator1MnRepository oneMnRepository;
    private final MicrostructureIndicator15MnRepository fifteenMnRepository;
    private final MicrostructureIndicator5MnRepository i5MnRepository;
    private final MicrostructureIndicator30MnRepository i30MnRepository;

    private final AmihudWarmupService amihudWarmupService;
    private final HasbrouckWarmupService hasbrouckWarmupService;
    private final KyleLambdaWarmupService kyleLambdaWarmupService;
    private final RangeWarmupService rangeWarmupService;
    private final RollWarmupService rollWarmupService;
    private final BodyWarmupService bodyWarmupService;
    private final WickWarmupService wickWarmupService;
    private final PositionBalanceWarmupService positionBalanceWarmupService;
    private final Return1CMicrostructureWarmupService return1CMicrostructureWarmupService;


    public void analyse(SymbolCandle candle, BarSeries series) {

        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var index = series.getEndIndex();

        log.info("[WARMUP][MIC] {} - {}", symbol, openTime);

        MicrostructureIndicatorEntity entity;

        switch (candle.getInterval()) {
            case I1_MN -> entity = oneMnRepository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewOneMn(candle));
            case I15_MN -> entity = fifteenMnRepository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewFifteenMn(candle));
            case I5_MN -> entity = i5MnRepository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew5m(candle));
            case I30_MN -> entity = i30MnRepository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew30m(candle));
            default -> throw new RuntimeException();
        }

        List<Callable<Void>> tasks = List.of(
                () -> { amihudWarmupService.analyse(entity, candle, series); return null; },
                () -> { bodyWarmupService.analyse(entity, candle, series); return null; },
                () -> { hasbrouckWarmupService.analyse(entity, candle, series); return null; },
                () -> { kyleLambdaWarmupService.analyse(entity, candle, series); return null; },
                () -> { positionBalanceWarmupService.analyse(entity, candle, series); return null; },
                () -> { rangeWarmupService.analyse(entity, candle, series); return null; },
                () -> { return1CMicrostructureWarmupService.analyse(entity, candle, series); return null; },
                // ReturnWindowMicrostructure removido — nenhuma mic_return_* na máscara
                () -> { rollWarmupService.analyse(entity, candle, series); return null; },
                // ShapePattern removido — nenhuma mic_candle_direction/type/shape/geometry na máscara
                () -> { wickWarmupService.analyse(entity, candle, series); return null; }
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
            case I1_MN -> oneMnRepository.save((MicrostructureIndicator1MnEntity) entity);
            case I15_MN -> fifteenMnRepository.save((MicrostructureIndicator15MnEntity) entity);
            case I5_MN ->  i5MnRepository.save((MicrostructureIndicator5MnEntity) entity);
            case I30_MN -> i30MnRepository.save((MicrostructureIndicator30MnEntity) entity);
            default -> throw new RuntimeException();
        }

    }

    private MicrostructureIndicator1MnEntity createNewOneMn(SymbolCandle candle) {
        var e = new MicrostructureIndicator1MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private MicrostructureIndicator15MnEntity createNewFifteenMn(SymbolCandle candle) {
        var e = new MicrostructureIndicator15MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private MicrostructureIndicator30MnEntity createNew30m(SymbolCandle candle) {
        var e = new MicrostructureIndicator30MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private MicrostructureIndicator5MnEntity createNew5m(SymbolCandle candle) {
        var e = new MicrostructureIndicator5MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }
}
