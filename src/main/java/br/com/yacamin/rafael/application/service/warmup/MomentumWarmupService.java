package br.com.yacamin.rafael.application.service.warmup;


import br.com.yacamin.rafael.application.service.warmup.momentum.*;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.MomentumIndicatorEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i15mn.MomentumIndicator15MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i30mn.MomentumIndicator30MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.MomentumIndicator5MnEntity;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.MomentumIndicator1MnEntity;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i15mn.MomentumIndicator15MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i30mn.MomentumIndicator30MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.MomentumIndicator5MnRepository;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.MomentumIndicator1MnRepository;
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
public class MomentumWarmupService {

    private final ExecutorService pool = Executors.newFixedThreadPool(32);

    private final MomentumIndicator15MnRepository i15mnRepository;
    private final MomentumIndicator30MnRepository i30mnRepository;
    private final MomentumIndicator1MnRepository i1mnRepository;
    private final MomentumIndicator5MnRepository i5mnRepository;

    private final CciWarmupService cciWarmupService;
    private final ClosePriceWarmupService closePriceWarmupService;
    private final CloseReturnWarmupService closeReturnWarmupService;
    private final CmoWarmupService cmoWarmupService;
    private final PpoWarmupService ppoWarmupService;
    private final RocWarmupService rocWarmupService;
    private final RsiWarmupService rsiWarmupService;
    private final StochWarmupService stochWarmupService;
    private final TrixWarmupService trixWarmupService;
    private final TsiWarmupService tsiWarmupService;
    private final WprWarmupService wprWarmupService;
    private final MomentumStabilityWarmupService momentumStabilityWarmupService;

    public void analyse(SymbolCandle candle, BarSeries series) {

        var symbol = candle.getSymbol();
        var openTime = candle.getOpenTime();
        var lastIndex = series.getEndIndex();

        log.info("[WARMUP][MOM] {} - {}", symbol, openTime);

        MomentumIndicatorEntity entity;

        switch (candle.getInterval()) {
            case I1_MN -> entity = i1mnRepository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewOneMn(candle));
            case I15_MN -> entity = i15mnRepository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNewFifteenMn(candle));
            case I5_MN -> entity = i5mnRepository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew5m(candle));
            case I30_MN -> entity = i30mnRepository.findBySymbolAndOpenTime(symbol, openTime).orElseGet(() -> createNew30m(candle));
            default -> throw new RuntimeException();
        }

        List<Callable<Void>> tasks = List.of(
                () -> { cciWarmupService.analyse(entity, candle, series); return null; },
                () -> { closePriceWarmupService.analyse(entity, candle, series); return null; },
                () -> { closeReturnWarmupService.analyse(entity, candle, series); return null; },
                () -> { cmoWarmupService.analyse(entity, candle, series); return null; },
                () -> { momentumStabilityWarmupService.analyse(entity, candle, series); return null; },
                () -> { ppoWarmupService.analyse(entity, candle, series); return null; },
                () -> { rocWarmupService.analyse(entity, candle, series); return null; },
                () -> { rsiWarmupService.analyse(entity, candle, series); return null; },
                () -> { stochWarmupService.analyse(entity, candle, series); return null; },
                () -> { trixWarmupService.analyse(entity, candle, series); return null; },
                () -> { tsiWarmupService.analyse(entity, candle, series); return null; },
                () -> { wprWarmupService.analyse(entity, candle, series); return null; }
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
            case I1_MN -> i1mnRepository.save((MomentumIndicator1MnEntity) entity);
            case I15_MN -> i15mnRepository.save((MomentumIndicator15MnEntity) entity);
            case I5_MN ->  i5mnRepository.save((MomentumIndicator5MnEntity) entity);
            case I30_MN -> i30mnRepository.save((MomentumIndicator30MnEntity) entity);
            default -> throw new RuntimeException();
        }
    }

    private MomentumIndicator1MnEntity createNewOneMn(SymbolCandle candle) {
        var e = new MomentumIndicator1MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private MomentumIndicator15MnEntity createNewFifteenMn(SymbolCandle candle) {
        var e = new MomentumIndicator15MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private MomentumIndicator30MnEntity createNew30m(SymbolCandle candle) {
        var e = new MomentumIndicator30MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }

    private MomentumIndicator5MnEntity createNew5m(SymbolCandle candle) {
        var e = new MomentumIndicator5MnEntity();
        e.setSymbol(candle.getSymbol());
        e.setOpenTime(candle.getOpenTime());
        return e;
    }
}
