package br.com.yacamin.rafael.application.service.candle;

import br.com.yacamin.rafael.adapter.out.persistence.mikhael.CandleMongoRepository;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.mongo.document.CandleDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Verifica sincronização dos candles no MongoDB.
 * Retorna o último openTime disponível para o warmup processar a partir dali.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncCheckService {

    private final CandleMongoRepository candleMongoRepository;

    /**
     * Retorna o último openTime de candles no MongoDB para o intervalo dado.
     * Retorna null se não houver candles (fresh start).
     */
    public Instant syncCheck(String symbol, CandleIntervals interval) {
        log.info("========== SYNC CHECK START [{}] ==========", interval);

        CandleDocument lastCandle = candleMongoRepository.findLatest(symbol, interval);

        if (lastCandle == null) {
            log.info("[SYNC] [{}] No candles in MongoDB — fresh start", interval);
            log.info("========== SYNC CHECK END [{}] ==========", interval);
            return null;
        }

        Instant lastTime = lastCandle.getOpenTime();
        log.info("[SYNC] [{}] Last candle at {} in collection {}", interval, lastTime,
                CandleMongoRepository.collectionName(interval));
        log.info("========== SYNC CHECK END [{}] ==========", interval);
        return lastTime;
    }
}
