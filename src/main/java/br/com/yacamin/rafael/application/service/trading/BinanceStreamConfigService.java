package br.com.yacamin.rafael.application.service.trading;

import br.com.yacamin.rafael.adapter.out.persistence.gabriel.BinanceStreamConfigRepository;
import br.com.yacamin.rafael.adapter.out.websocket.binance.SpotMarketDataWsAdapter;
import br.com.yacamin.rafael.domain.BinanceStreamConfig;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceStreamConfigService {

    private final BinanceStreamConfigRepository repository;
    private final SpotMarketDataWsAdapter spotMarketDataWsAdapter;

    private final Set<String> subscribedStreams = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, ExecutorService> symbolExecutors = new ConcurrentHashMap<>();

    public void initOnStartup() {
        List<BinanceStreamConfig> all = repository.findAll();
        if (all.isEmpty()) {
            BinanceStreamConfig seed = BinanceStreamConfig.builder()
                    .streamCode("btcusdt@bookTicker")
                    .active(true)
                    .build();
            repository.save(seed);
            all = List.of(seed);
            log.info("[BinanceStreamConfig] Seeded default: btcusdt@bookTicker");
        }

        List<String> toSubscribe = new ArrayList<>();
        for (BinanceStreamConfig cfg : all) {
            if (cfg.isActive()) {
                toSubscribe.add(cfg.getStreamCode());
                createExecutor(extractSymbol(cfg.getStreamCode()));
            }
        }

        if (!toSubscribe.isEmpty()) {
            spotMarketDataWsAdapter.subscribe(toSubscribe);
            subscribedStreams.addAll(toSubscribe);
            log.info("[BinanceStreamConfig] Subscribed to {} streams on startup", toSubscribe.size());
        }
    }

    public List<Map<String, Object>> listAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (BinanceStreamConfig cfg : repository.findAll()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", cfg.getId());
            row.put("streamCode", cfg.getStreamCode());
            row.put("active", cfg.isActive());
            row.put("subscribed", subscribedStreams.contains(cfg.getStreamCode()));
            result.add(row);
        }
        return result;
    }

    public BinanceStreamConfig add(String symbol) {
        String streamCode = symbol.toLowerCase() + "@bookTicker";
        Optional<BinanceStreamConfig> existing = repository.findByStreamCode(streamCode);
        if (existing.isPresent()) return existing.get();

        BinanceStreamConfig cfg = BinanceStreamConfig.builder()
                .streamCode(streamCode)
                .active(false)
                .build();
        return repository.save(cfg);
    }

    public void start(String id) {
        repository.findById(id).ifPresent(cfg -> {
            createExecutor(extractSymbol(cfg.getStreamCode()));
            spotMarketDataWsAdapter.subscribe(List.of(cfg.getStreamCode()));
            subscribedStreams.add(cfg.getStreamCode());
            cfg.setActive(true);
            repository.save(cfg);
            log.info("[BinanceStreamConfig] Started stream: {}", cfg.getStreamCode());
        });
    }

    public void pause(String id) {
        repository.findById(id).ifPresent(cfg -> {
            spotMarketDataWsAdapter.unsubscribe(List.of(cfg.getStreamCode()));
            subscribedStreams.remove(cfg.getStreamCode());
            shutdownExecutor(extractSymbol(cfg.getStreamCode()));
            cfg.setActive(false);
            repository.save(cfg);
            log.info("[BinanceStreamConfig] Paused stream: {}", cfg.getStreamCode());
        });
    }

    public void remove(String id) {
        repository.findById(id).ifPresent(cfg -> {
            if (subscribedStreams.contains(cfg.getStreamCode())) {
                spotMarketDataWsAdapter.unsubscribe(List.of(cfg.getStreamCode()));
                subscribedStreams.remove(cfg.getStreamCode());
            }
            shutdownExecutor(extractSymbol(cfg.getStreamCode()));
            repository.delete(cfg);
            log.info("[BinanceStreamConfig] Removed stream: {}", cfg.getStreamCode());
        });
    }

    public ExecutorService getExecutor(String symbol) {
        return symbolExecutors.get(symbol.toLowerCase());
    }

    public String extractSymbol(String streamCode) {
        int idx = streamCode.indexOf('@');
        return idx > 0 ? streamCode.substring(0, idx) : streamCode;
    }

    private void createExecutor(String symbol) {
        symbolExecutors.computeIfAbsent(symbol, s -> {
            log.info("[BinanceStreamConfig] Created executor: tick-{}", s);
            return Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "tick-" + s);
                t.setDaemon(true);
                return t;
            });
        });
    }

    private void shutdownExecutor(String symbol) {
        ExecutorService exec = symbolExecutors.remove(symbol);
        if (exec != null) {
            exec.shutdown();
            log.info("[BinanceStreamConfig] Shutdown executor: tick-{}", symbol);
        }
    }

    @PreDestroy
    public void shutdown() {
        symbolExecutors.values().forEach(ExecutorService::shutdown);
        symbolExecutors.clear();
    }
}
