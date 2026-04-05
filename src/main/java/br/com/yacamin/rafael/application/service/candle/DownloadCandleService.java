package br.com.yacamin.rafael.application.service.candle;

import br.com.yacamin.rafael.adapter.out.persistence.mikhael.CandleMongoRepository;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.SymbolCandle;
import br.com.yacamin.rafael.domain.mongo.document.CandleDocument;
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

    private final CandleMongoRepository candleMongoRepository;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;

    @Value("${binance.paths.spot.rest}")
    private String binanceBasePath;

    @Value("${binance.endpoints.spot.klines}")
    private String binanceKlinesEndpoint;

    public DownloadCandleService(CandleMongoRepository candleMongoRepository,
                                  ObjectMapper objectMapper,
                                  OkHttpClient okHttpClient) {
        this.candleMongoRepository = candleMongoRepository;
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

    private void persist(List<SymbolCandle> candles, CandleIntervals interval) {
        var filtered = candles.stream()
                .filter(c -> c.getNumberOfTrades() > 0)
                .toList();

        if (filtered.isEmpty()) return;

        long t0 = System.currentTimeMillis();

        List<CandleDocument> documents = filtered.stream()
                .map(SymbolCandle::toCandleDocument)
                .toList();

        log.info("[DOWNLOAD] Saving {} candle documents to {}...", documents.size(), CandleMongoRepository.collectionName(interval));
        candleMongoRepository.saveAll(documents, interval);

        log.info("[DOWNLOAD] Persisted {} candles [{}] in {}ms", filtered.size(), interval, System.currentTimeMillis() - t0);
    }
}
