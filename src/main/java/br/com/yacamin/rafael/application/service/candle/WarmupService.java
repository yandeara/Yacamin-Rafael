package br.com.yacamin.rafael.application.service.candle;

import br.com.yacamin.rafael.adapter.out.persistence.mikhael.CandleMongoRepository;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.mongo.document.CandleDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarmupService {

    // Mínimo de bars no BarSeries pra triggar AnalyseOrchestrator (calcular indicadores)
    private static final int MIN_REQUIRED = 1000;

    // Baixar o dobro: primeiros 1000 aquecem os indicadores, últimos 1000 geram features no banco.
    // Garante que quando o primeiro candle live chegar, há 1000 indicadores sólidos.
    private static final int DOWNLOAD_DEPTH = 2000;

    private final CandleMongoRepository candleMongoRepository;
    private final BarSeriesCacheService barSeriesCacheService;
    private final DownloadCandleService downloadCandleService;
    private final SyncCheckService syncCheckService;

    public void warmup(String symbol, CandleIntervals interval) {
        long t0 = System.currentTimeMillis();
        log.info("========== WARMUP START [{}] ==========", interval);
        log.info("[WARMUP] symbol={}, interval={}", symbol, interval);

        // 0) Sync check
        Instant syncPoint = syncCheckService.syncCheck(symbol, interval);

        // 1) Descobrir ultimo candle no banco
        CandleDocument lastCandle = candleMongoRepository.findLatest(symbol, interval);

        long downloadMinutes = DOWNLOAD_DEPTH * (interval.getDuration().toMinutes());

        if (lastCandle == null) {
            log.warn("[WARMUP] No candles in MongoDB for {} [{}] — downloading {} candles of history",
                    symbol, interval, DOWNLOAD_DEPTH);
            Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
            Instant start = now.minus(downloadMinutes, ChronoUnit.MINUTES);
            downloadCandleService.download(interval, symbol, start, now);
            lastCandle = candleMongoRepository.findLatest(symbol, interval);
            if (lastCandle == null) {
                log.error("[WARMUP] Still no candles after download — aborting");
                return;
            }
        }

        Instant endDate = syncPoint != null ? syncPoint : lastCandle.getOpenTime();
        Instant startDate = endDate.minus(downloadMinutes, ChronoUnit.MINUTES);

        // 2) Phase 1: Carregar 2000 candles historicos + calcular features
        //    Primeiros ~1000 aquecem indicadores, últimos ~1000 geram features no banco
        log.info("[WARMUP] Phase 1: Load {} candles {} -> {}", DOWNLOAD_DEPTH, startDate, endDate);
        int loaded = warmupProcess(symbol, interval, startDate, endDate);

        var series = barSeriesCacheService.get(symbol, interval);
        int barCount = series != null ? series.getBarCount() : 0;
        log.info("[WARMUP] Phase 1 done: {} loaded, BarSeries={} bars", loaded, barCount);

        // 3) Phase 2: Baixar gap ate agora
        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        if (endDate.isBefore(now.minus(interval.getDuration()))) {
            log.info("[WARMUP] Phase 2: Download gap {} -> {}", endDate, now);
            long dlStart = System.currentTimeMillis();
            downloadCandleService.download(interval, symbol, endDate, now);
            log.info("[WARMUP] Phase 2 download took {}ms", System.currentTimeMillis() - dlStart);

            // 4) Phase 3: Processar gap com features
            Instant gapStart = endDate.plus(interval.getDuration());
            log.info("[WARMUP] Phase 3: Process gap candles {} -> {}", gapStart, now);
            int gapLoaded = warmupProcess(symbol, interval, gapStart, now);
            log.info("[WARMUP] Phase 3 done: {} gap candles processed", gapLoaded);
        } else {
            log.info("[WARMUP] No gap — already up to date");
        }

        series = barSeriesCacheService.get(symbol, interval);
        barCount = series != null ? series.getBarCount() : 0;
        long elapsed = System.currentTimeMillis() - t0;
        log.info("[WARMUP] COMPLETE [{}]: BarSeries={} bars, total={}ms", interval, barCount, elapsed);
        log.info("========== WARMUP END [{}] ==========", interval);
    }

    /**
     * Le candles do MongoDB, verifica integridade (sem gaps), e alimenta BarSeries.
     * Se encontrar gaps, baixa os candles faltantes antes de processar.
     */
    private int warmupProcess(String symbol, CandleIntervals interval, Instant start, Instant end) {
        List<CandleDocument> candles = candleMongoRepository.findByRange(symbol, start, end, interval);

        log.info("[WARMUP] warmupProcess [{}]: {} candles from MongoDB ({} -> {})", interval, candles.size(), start, end);

        if (candles.isEmpty()) {
            log.warn("[WARMUP] warmupProcess: no candles in range");
            return 0;
        }

        // Integrity check — detectar e preencher gaps
        candles = integrityCheck(symbol, interval, candles, start, end);

        log.info("[WARMUP] warmupProcess: first={}, last={}, count={}",
                candles.getFirst().getOpenTime(), candles.getLast().getOpenTime(), candles.size());

        int loaded = 0;
        int skipped = 0;
        for (CandleDocument doc : candles) {
            SymbolCandle candle = SymbolCandle.fromCandleDocument(doc, interval);
            if (candle.getNumberOfTrades() > 0) {
                barSeriesCacheService.update(symbol, interval, candle, false);
                loaded++;
            } else {
                skipped++;
            }
        }

        if (skipped > 0) {
            log.info("[WARMUP] warmupProcess: {} loaded, {} skipped (empty)", loaded, skipped);
        }

        return loaded;
    }

    /**
     * Verifica integridade sequencial dos candles.
     * Se faltar candles, baixa do Binance.
     * Usa Set<Instant> para O(1) lookup — eficiente mesmo com 1000+ candles.
     */
    private List<CandleDocument> integrityCheck(String symbol, CandleIntervals interval,
                                                 List<CandleDocument> candles,
                                                 Instant start, Instant end) {
        long stepSeconds = interval.getDuration().toSeconds();
        long expectedCount = Duration.between(start, end).toSeconds() / stepSeconds + 1;

        if (candles.size() >= expectedCount) {
            log.info("[INTEGRITY] OK: {}/{} candles present (no gaps)", candles.size(), expectedCount);
            return candles;
        }

        log.warn("[INTEGRITY] Gap detected: {}/{} candles present — {} missing",
                candles.size(), expectedCount, expectedCount - candles.size());

        // Build Set de openTimes existentes para O(1) lookup
        Set<Instant> existing = new HashSet<>(candles.size());
        for (CandleDocument c : candles) {
            existing.add(c.getOpenTime());
        }

        // Scan linear para achar gaps
        List<Instant> missing = new ArrayList<>();
        Instant cursor = start;
        while (!cursor.isAfter(end)) {
            if (!existing.contains(cursor)) {
                missing.add(cursor);
            }
            cursor = cursor.plusSeconds(stepSeconds);
        }

        if (missing.isEmpty()) {
            log.info("[INTEGRITY] No actual gaps found after scan (count mismatch was cosmetic)");
            return candles;
        }

        // Agrupar gaps consecutivos em ranges para download eficiente
        List<Instant[]> gapRanges = groupConsecutiveGaps(missing, stepSeconds);

        log.warn("[INTEGRITY] {} missing candles in {} gap ranges:", missing.size(), gapRanges.size());
        for (Instant[] range : gapRanges) {
            long gapCount = Duration.between(range[0], range[1]).toSeconds() / stepSeconds + 1;
            log.warn("[INTEGRITY]   {} -> {} ({} candles)", range[0], range[1], gapCount);
        }

        // Download cada range de gap
        for (Instant[] range : gapRanges) {
            downloadCandleService.download(interval, symbol, range[0], range[1]);
        }

        // Re-ler do MongoDB apos preencher gaps
        List<CandleDocument> refreshed = candleMongoRepository.findByRange(symbol, start, end, interval);

        long stillMissing = expectedCount - refreshed.size();
        if (stillMissing > 0) {
            log.warn("[INTEGRITY] After fill: still {} candles missing (may be weekend/maintenance gaps)",
                    stillMissing);
        } else {
            log.info("[INTEGRITY] All gaps filled successfully: {}/{}", refreshed.size(), expectedCount);
        }

        return refreshed;
    }

    /**
     * Agrupa Instants consecutivos em ranges [start, end] para download batch.
     * Ex: [15:10, 15:11, 15:12, 15:20, 15:21] → [[15:10, 15:12], [15:20, 15:21]]
     */
    private List<Instant[]> groupConsecutiveGaps(List<Instant> missing, long stepSeconds) {
        List<Instant[]> ranges = new ArrayList<>();
        Instant rangeStart = missing.getFirst();
        Instant prev = rangeStart;

        for (int i = 1; i < missing.size(); i++) {
            Instant curr = missing.get(i);
            if (Duration.between(prev, curr).toSeconds() > stepSeconds) {
                ranges.add(new Instant[]{rangeStart, prev});
                rangeStart = curr;
            }
            prev = curr;
        }
        ranges.add(new Instant[]{rangeStart, prev});

        return ranges;
    }
}
