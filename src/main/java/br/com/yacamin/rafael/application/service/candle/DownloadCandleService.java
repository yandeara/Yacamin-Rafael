package br.com.yacamin.rafael.application.service.candle;

import br.com.yacamin.rafael.adapter.out.persistence.Candle1MnRepository;
import br.com.yacamin.rafael.adapter.out.persistence.Candle5MnRepository;
import br.com.yacamin.rafael.adapter.out.rest.binance.BinanceSpotRestClient;
import br.com.yacamin.rafael.adapter.out.rest.binance.KlineRequest;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.SymbolCandle;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadCandleService {

    private final BinanceSpotRestClient binanceSpotRestClient;
    private final Candle1MnRepository candle1MnRepository;
    private final Candle5MnRepository candle5MnRepository;
    private final ObjectMapper objectMapper;

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
            KlineRequest request = new KlineRequest();
            request.setSymbol(symbol);
            request.setInterval(interval.getValue());
            request.setLimit(1440);
            request.setStartTime(start.toEpochMilli());
            request.setEndTime(end.toEpochMilli());

            String json = binanceSpotRestClient.klines(request).get();
            List<List<Object>> rawCandles = objectMapper.readValue(json, new TypeReference<>() {});

            return SymbolCandle.parseCandles(symbol, interval, rawCandles);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao baixar candles de " + symbol, e);
        }
    }

    private void persist(List<SymbolCandle> candles, CandleIntervals interval) {
        var filtered = candles.stream()
                .filter(c -> c.getNumberOfTrades() > 0)
                .toList();

        if (filtered.isEmpty()) return;

        switch (interval) {
            case I1_MN -> {
                var entities = filtered.stream().map(SymbolCandle::toCandle1Mn).toList();
                candle1MnRepository.saveAll(entities);
            }
            case I5_MN -> {
                var entities = filtered.stream().map(SymbolCandle::toCandle5Mn).toList();
                candle5MnRepository.saveAll(entities);
            }
            default -> { return; }
        }

        log.debug("[DOWNLOAD] Persisted {} candles [{}] to Scylla", filtered.size(), interval);
    }
}
