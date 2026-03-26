package br.com.yacamin.rafael.application.service.candle;

import br.com.yacamin.rafael.adapter.out.persistence.Candle1MnRepository;
import br.com.yacamin.rafael.adapter.out.persistence.Candle5MnRepository;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.SymbolCandle;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DownloadCandleService {

    private final Candle1MnRepository candle1MnRepository;
    private final Candle5MnRepository candle5MnRepository;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;

    @Value("${binance.paths.spot.rest}")
    private String binanceBasePath;

    @Value("${binance.endpoints.spot.klines}")
    private String binanceKlinesEndpoint;

    public DownloadCandleService(Candle1MnRepository candle1MnRepository,
                                  Candle5MnRepository candle5MnRepository,
                                  ObjectMapper objectMapper,
                                  OkHttpClient okHttpClient) {
        this.candle1MnRepository = candle1MnRepository;
        this.candle5MnRepository = candle5MnRepository;
        this.objectMapper = objectMapper;
        this.okHttpClient = okHttpClient;
    }

    public List<SymbolCandle> download(CandleIntervals interval, String symbol, Instant start, Instant end) {
        log.info("[DOWNLOAD] Starting: {} [{}] from {} to {}", symbol, interval, start, end);
        List<SymbolCandle> allCandles = new ArrayList<>();

        var chunk = interval.getChunk();
        Instant cursor = start;
        int chunkNum = 0;

        while (cursor.isBefore(end)) {
            Instant chunkEnd = cursor.plus(chunk.value(), chunk.unit());
            if (chunkEnd.isAfter(end)) chunkEnd = end;
            chunkNum++;

            try {
                List<SymbolCandle> batch = downloadChunk(symbol, interval, cursor, chunkEnd);
                allCandles.addAll(batch);
                persist(batch, interval);
                log.debug("[DOWNLOAD] Chunk {}: {} -> {}, got {} candles", chunkNum, cursor, chunkEnd, batch.size());
            } catch (Exception e) {
                log.error("[DOWNLOAD] Chunk {} FAILED: {} [{} -> {}]: {}", chunkNum, symbol, cursor, chunkEnd, e.getMessage());
            }

            cursor = chunkEnd;
        }

        log.info("[DOWNLOAD] Complete: {} total candles of {} [{}] in {} chunks", allCandles.size(), symbol, interval, chunkNum);
        return allCandles;
    }

    private List<SymbolCandle> downloadChunk(String symbol, CandleIntervals interval, Instant start, Instant end) {
        try {
            String url = binanceBasePath + binanceKlinesEndpoint
                    + "?symbol=" + symbol
                    + "&interval=" + interval.getValue()
                    + "&limit=1440"
                    + "&startTime=" + start.toEpochMilli()
                    + "&endTime=" + end.toEpochMilli();

            okhttp3.Request req = new okhttp3.Request.Builder().url(url).get().build();
            try (okhttp3.Response response = okHttpClient.newCall(req).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Binance REST error: " + response.code());
                }
                String json = response.body() != null ? response.body().string() : "[]";
                List<List<Object>> rawCandles = objectMapper.readValue(json, new TypeReference<>() {});
                return SymbolCandle.parseCandles(symbol, interval, rawCandles);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao baixar candles de " + symbol, e);
        }
    }

    private static final int BATCH_SIZE = 50;

    private void persist(List<SymbolCandle> candles, CandleIntervals interval) {
        var filtered = candles.stream()
                .filter(c -> c.getNumberOfTrades() > 0)
                .toList();

        if (filtered.isEmpty()) return;

        long t0 = System.currentTimeMillis();

        switch (interval) {
            case I1_MN -> {
                var entities = filtered.stream().map(SymbolCandle::toCandle1Mn).toList();
                log.info("[DOWNLOAD] Saving {} candle_1_mn entities...", entities.size());
                for (int i = 0; i < entities.size(); i += BATCH_SIZE) {
                    int end = Math.min(i + BATCH_SIZE, entities.size());
                    try {
                        candle1MnRepository.saveAll(entities.subList(i, end));
                        log.debug("[DOWNLOAD] Batch {}-{} saved", i, end);
                    } catch (Exception e) {
                        log.error("[DOWNLOAD] Batch {}-{} FAILED: {}", i, end, e.getMessage(), e);
                        throw e;
                    }
                }
            }
            case I5_MN -> {
                var entities = filtered.stream().map(SymbolCandle::toCandle5Mn).toList();
                log.info("[DOWNLOAD] Saving {} candle_5_mn entities...", entities.size());
                for (int i = 0; i < entities.size(); i += BATCH_SIZE) {
                    int end = Math.min(i + BATCH_SIZE, entities.size());
                    try {
                        candle5MnRepository.saveAll(entities.subList(i, end));
                        log.debug("[DOWNLOAD] Batch {}-{} saved", i, end);
                    } catch (Exception e) {
                        log.error("[DOWNLOAD] Batch {}-{} FAILED: {}", i, end, e.getMessage(), e);
                        throw e;
                    }
                }
            }
            default -> { return; }
        }

        log.info("[DOWNLOAD] Persisted {} candles [{}] in {}ms", filtered.size(), interval, System.currentTimeMillis() - t0);
    }
}
