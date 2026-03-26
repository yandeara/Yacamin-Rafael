package br.com.yacamin.rafael.application.service.candle;

import br.com.yacamin.rafael.adapter.out.persistence.Candle1MnRepository;
import br.com.yacamin.rafael.adapter.out.persistence.Candle5MnRepository;
import br.com.yacamin.rafael.domain.CandleIntervals;
import br.com.yacamin.rafael.domain.scylla.entity.Candle1Mn;
import br.com.yacamin.rafael.domain.scylla.entity.Candle5Mn;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i1mn.*;
import br.com.yacamin.rafael.domain.scylla.entity.indicator.i5mn.*;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i1mn.*;
import br.com.yacamin.rafael.domain.scylla.repository.indicator.i5mn.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Verifica sincronização entre candle e todas as tabelas de indicadores.
 * Se alguma tabela estiver dessincronizada, encerra a aplicação com erro
 * mostrando onde está a quebra de integridade.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncCheckService {

    private final Candle1MnRepository candle1MnRepository;
    private final Candle5MnRepository candle5MnRepository;

    // 1mn repos
    private final MomentumIndicator1MnRepository momentum1MnRepo;
    private final VolatilityIndicator1MnRepository volatility1MnRepo;
    private final MicrostructureIndicator1MnRepository microstructure1MnRepo;
    private final TimeIndicator1MnRepository time1MnRepo;
    private final TrendIndicator1MnRepository trend1MnRepo;
    private final VolumeIndicator1MnRepository volume1MnRepo;

    // 5mn repos
    private final MomentumIndicator5MnRepository momentum5MnRepo;
    private final VolatilityIndicator5MnRepository volatility5MnRepo;
    private final MicrostructureIndicator5MnRepository microstructure5MnRepo;
    private final TimeIndicator5MnRepository time5MnRepo;
    private final TrendIndicator5MnRepository trend5MnRepo;
    private final VolumeIndicator5MnRepository volume5MnRepo;

    /**
     * Retorna o Instant a partir do qual o warmup deve processar.
     * Se todas as tabelas estiverem sincronizadas, retorna o último openTime dos candles.
     * Se houver dessincronia, lança IllegalStateException e encerra a aplicação.
     */
    public Instant syncCheck(String symbol, CandleIntervals interval) {
        log.info("========== SYNC CHECK START [{}] ==========", interval);

        Map<String, Instant> lastTimes = new LinkedHashMap<>();

        findLastCandle(lastTimes, symbol, interval);
        findLastIndicator(lastTimes, "momentum", symbol, interval);
        findLastIndicator(lastTimes, "volatility", symbol, interval);
        findLastIndicator(lastTimes, "microstructure", symbol, interval);
        findLastIndicator(lastTimes, "time", symbol, interval);
        findLastIndicator(lastTimes, "trend", symbol, interval);
        findLastIndicator(lastTimes, "volume", symbol, interval);

        lastTimes.forEach((table, time) ->
                log.info("[SYNC] [{}] {} -> last openTime: {}", interval, table, time != null ? time : "EMPTY"));

        Map<String, Instant> nonEmpty = new LinkedHashMap<>();
        lastTimes.forEach((k, v) -> { if (v != null) nonEmpty.put(k, v); });

        if (nonEmpty.isEmpty()) {
            log.info("[SYNC] [{}] All tables empty — fresh start", interval);
            log.info("========== SYNC CHECK END [{}] ==========", interval);
            return null;
        }

        Instant oldest = Collections.min(nonEmpty.values());
        Instant newest = Collections.max(nonEmpty.values());

        if (oldest.equals(newest)) {
            log.info("[SYNC] [{}] All tables synchronized at {}", interval, newest);
            log.info("========== SYNC CHECK END [{}] ==========", interval);
            return newest;
        }

        log.error("[SYNC] [{}] !!!! INTEGRITY BREAK DETECTED !!!! oldest={}, newest={}", interval, oldest, newest);
        nonEmpty.forEach((table, time) -> {
            if (time.equals(oldest)) {
                log.error("[SYNC]   {} -> {} (OLDEST - lagging behind)", table, time);
            } else if (!time.equals(newest)) {
                log.error("[SYNC]   {} -> {} (behind)", table, time);
            } else {
                log.error("[SYNC]   {} -> {} (ok)", table, time);
            }
        });

        lastTimes.forEach((table, time) -> {
            if (time == null) {
                log.error("[SYNC]   {} -> EMPTY (missing data)", table);
            }
        });

        log.error("[SYNC] [{}] Tables are NOT synchronized. Expected all at {} but found discrepancies.", interval, newest);
        log.error("[SYNC] Shutting down to prevent corrupted data processing.");
        log.error("========== SYNC CHECK END [{}] ==========", interval);
        throw new IllegalStateException(
                "Integrity check failed for symbol [" + symbol + "] interval [" + interval + "]: tables are desynchronized. " +
                "Expected all tables at " + newest + " but oldest is at " + oldest + ". " +
                "Fix the data manually before restarting.");
    }

    private void findLastCandle(Map<String, Instant> map, String symbol, CandleIntervals interval) {
        String tableName = switch (interval) {
            case I1_MN -> "candle_1_mn";
            case I5_MN -> "candle_5_mn";
            default -> throw new IllegalArgumentException("Interval not supported: " + interval);
        };
        try {
            Instant last = switch (interval) {
                case I1_MN -> {
                    Candle1Mn c = candle1MnRepository.findFirstBySymbolOrderByOpenTimeDesc(symbol);
                    yield c != null ? c.getOpenTime() : null;
                }
                case I5_MN -> {
                    Candle5Mn c = candle5MnRepository.findFirstBySymbolOrderByOpenTimeDesc(symbol);
                    yield c != null ? c.getOpenTime() : null;
                }
                default -> null;
            };
            map.put(tableName, last);
        } catch (Exception e) {
            log.error("[SYNC] Error querying {}: {}", tableName, e.getMessage());
            map.put(tableName, null);
        }
    }

    private void findLastIndicator(Map<String, Instant> map, String category, String symbol, CandleIntervals interval) {
        try {
            Instant last = switch (interval) {
                case I1_MN -> findLastIndicator1Mn(category, symbol);
                case I5_MN -> findLastIndicator5Mn(category, symbol);
                default -> null;
            };
            map.put(category, last);
        } catch (Exception e) {
            log.error("[SYNC] Error querying {} [{}]: {}", category, interval, e.getMessage());
            map.put(category, null);
        }
    }

    private Instant findLastIndicator1Mn(String category, String symbol) {
        return switch (category) {
            case "momentum" -> momentum1MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(MomentumIndicator1MnEntity::getOpenTime).orElse(null);
            case "volatility" -> volatility1MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(VolatilityIndicator1MnEntity::getOpenTime).orElse(null);
            case "microstructure" -> microstructure1MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(MicrostructureIndicator1MnEntity::getOpenTime).orElse(null);
            case "time" -> time1MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(TimeIndicator1MnEntity::getOpenTime).orElse(null);
            case "trend" -> trend1MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(TrendIndicator1MnEntity::getOpenTime).orElse(null);
            case "volume" -> volume1MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(VolumeIndicator1MnEntity::getOpenTime).orElse(null);
            default -> null;
        };
    }

    private Instant findLastIndicator5Mn(String category, String symbol) {
        return switch (category) {
            case "momentum" -> momentum5MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(MomentumIndicator5MnEntity::getOpenTime).orElse(null);
            case "volatility" -> volatility5MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(VolatilityIndicator5MnEntity::getOpenTime).orElse(null);
            case "microstructure" -> microstructure5MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(MicrostructureIndicator5MnEntity::getOpenTime).orElse(null);
            case "time" -> time5MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(TimeIndicator5MnEntity::getOpenTime).orElse(null);
            case "trend" -> trend5MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(TrendIndicator5MnEntity::getOpenTime).orElse(null);
            case "volume" -> volume5MnRepo.findFirstBySymbolOrderByOpenTimeDesc(symbol)
                    .map(VolumeIndicator5MnEntity::getOpenTime).orElse(null);
            default -> null;
        };
    }
}
