package br.com.yacamin.rafael.application.service.analyse;

import br.com.yacamin.rafael.application.service.warmup.*;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.CandleIntervals;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyseOrchestratorService {

    private final MicrostructureWarmupService microstructureWarmupService;
    private final TimeWarmupService timeWarmupService;
    private final TrendWarmupService trendWarmupService;
    private final MomentumWarmupService momentumWarmupService;
    private final VolatilityWarmupService volatilityWarmupService;
    private final VolumeWarmupService volumeWarmupService;

    private final ExecutorService pool = Executors.newFixedThreadPool(6);

    public void analyse(SymbolCandle candle, CandleIntervals interval, BarSeries series) {
        long t0 = System.currentTimeMillis();
        log.debug("[ANALYSE] START {} [{}] @ {} (bars={})", candle.getSymbol(), interval, candle.getOpenTime(), series.getBarCount());

        List<Callable<Void>> tasks = List.of(
                wrap("MICRO", () -> microstructureWarmupService.analyse(candle, series)),
                wrap("MOM",   () -> momentumWarmupService.analyse(candle, series)),
                wrap("TIME",  () -> timeWarmupService.analyse(candle, series)),
                wrap("TREND", () -> trendWarmupService.analyse(candle, series)),
                wrap("VOL",   () -> volatilityWarmupService.analyse(candle, series)),
                wrap("VOLUME",() -> volumeWarmupService.analyse(candle, series))
                // TPSL removido — nenhuma feature tpsl_ na máscara getTpSlMask()
        );

        try {
            var futures = pool.invokeAll(tasks);

            for (var f : futures) {
                try {
                    f.get();
                } catch (ExecutionException ee) {
                    log.error("[ANALYSE] Error in indicator group for {} @ {}: {}",
                            candle.getSymbol(), candle.getOpenTime(), ee.getCause().getMessage(), ee.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        long elapsed = System.currentTimeMillis() - t0;
        log.debug("[ANALYSE] DONE {} @ {} in {}ms", candle.getSymbol(), candle.getOpenTime(), elapsed);
    }

    private Callable<Void> wrap(String group, Runnable task) {
        return () -> {
            long t0 = System.currentTimeMillis();
            try {
                task.run();
                log.trace("[ANALYSE][{}] completed in {}ms", group, System.currentTimeMillis() - t0);
            } catch (Exception e) {
                log.error("[ANALYSE][{}] FAILED after {}ms: {}", group, System.currentTimeMillis() - t0, e.getMessage(), e);
                throw e;
            }
            return null;
        };
    }
}
