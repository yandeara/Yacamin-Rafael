package br.com.yacamin.rafael.application.service.model;

import br.com.yacamin.rafael.domain.InferencePrediction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena em memória as inferências por bloco de 5 minutos.
 *
 * Três tipos de inferência:
 * 1. Minute-by-minute (M2M): lista de até 5 predictions por bloco (candles 1m, h1)
 * 2. Block-by-block (B2B): 1 prediction por bloco (candle 5m prevê o próximo bloco, h1)
 * 3. Horizon (H4): 1 prediction por bloco (candle 1m min1 prevê o fim do mesmo bloco, h4)
 */
@Slf4j
@Service
public class BlockInferenceMemoryService {

    private static final int MAX_BLOCKS = 100;

    // Minute-by-minute: blockUnix → lista de até 5 predictions
    private final Map<Long, List<InferencePrediction>> m2mInferences = new ConcurrentHashMap<>();
    private final Deque<Long> m2mOrder = new ArrayDeque<>();

    // Block-by-block: blockUnix → 1 prediction (o candle 5m prevê ESTE bloco)
    private final Map<Long, InferencePrediction> b2bInferences = new ConcurrentHashMap<>();
    private final Deque<Long> b2bOrder = new ArrayDeque<>();

    // ── Minute-by-minute ──

    public synchronized void addPrediction(long blockUnix, InferencePrediction prediction) {
        m2mInferences.computeIfAbsent(blockUnix, k -> {
            m2mOrder.addLast(k);
            evict(m2mOrder, m2mInferences);
            return Collections.synchronizedList(new ArrayList<>(5));
        }).add(prediction);

        log.debug("[M2M] Block {} min {}: {} ({})",
                blockUnix, prediction.getMinuteInBlock(), prediction.getDirection(), prediction.getConfidence());
    }

    public List<InferencePrediction> getPredictions(long blockUnix) {
        return m2mInferences.getOrDefault(blockUnix, List.of());
    }

    // ── Block-by-block ──

    public synchronized void addBlockPrediction(long blockUnix, InferencePrediction prediction) {
        if (!b2bInferences.containsKey(blockUnix)) {
            b2bOrder.addLast(blockUnix);
            evict(b2bOrder, b2bInferences);
        }
        b2bInferences.put(blockUnix, prediction);

        log.debug("[B2B] Block {}: {} ({})",
                blockUnix, prediction.getDirection(), prediction.getConfidence());
    }

    public InferencePrediction getBlockPrediction(long blockUnix) {
        return b2bInferences.get(blockUnix);
    }

    // ── Horizon (H4) ──

    private final Map<Long, InferencePrediction> h4Inferences = new ConcurrentHashMap<>();
    private final Deque<Long> h4Order = new ArrayDeque<>();

    public synchronized void addHorizonPrediction(long blockUnix, InferencePrediction prediction) {
        if (!h4Inferences.containsKey(blockUnix)) {
            h4Order.addLast(blockUnix);
            evict(h4Order, h4Inferences);
        }
        h4Inferences.put(blockUnix, prediction);

        log.debug("[H4] Block {}: {} ({})",
                blockUnix, prediction.getDirection(), prediction.getConfidence());
    }

    public InferencePrediction getHorizonPrediction(long blockUnix) {
        return h4Inferences.get(blockUnix);
    }

    // ── Eviction ──

    private <V> void evict(Deque<Long> order, Map<Long, V> map) {
        while (order.size() > MAX_BLOCKS) {
            Long oldest = order.pollFirst();
            if (oldest != null) {
                map.remove(oldest);
            }
        }
    }
}
