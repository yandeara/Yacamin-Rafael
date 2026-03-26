package br.com.yacamin.rafael.application.service.trading;

import br.com.yacamin.rafael.adapter.out.persistence.MarketGroupRepository;
import br.com.yacamin.rafael.domain.BlockDuration;
import br.com.yacamin.rafael.domain.MarketGroup;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketGroupService {

    private final MarketGroupRepository repository;

    private final ConcurrentHashMap<String, MarketGroup> activeGroups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ExecutorService> groupExecutors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> streamToGroups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> displayNames = new ConcurrentHashMap<>();

    public void initOnStartup() {
        List<MarketGroup> all = repository.findAll();
        if (all.isEmpty()) {
            MarketGroup seed = MarketGroup.builder()
                    .slugPrefix("btc-updown-5m-")
                    .displayName("BTC 5m")
                    .blockDuration(BlockDuration.FIVE_MIN)
                    .binanceStream("btcusdt@bookTicker")
                    .active(true)
                    .build();
            repository.save(seed);
            all = List.of(seed);
            log.info("[MarketGroup] Seeded default: btc-updown-5m-");
        }

        for (MarketGroup mg : all) {
            if (mg.isActive()) {
                startInternal(mg);
            }
        }
        log.info("[MarketGroup] Loaded {} active groups on startup", activeGroups.size());
    }

    public List<Map<String, Object>> listAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (MarketGroup mg : repository.findAll()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", mg.getId());
            row.put("slugPrefix", mg.getSlugPrefix());
            row.put("displayName", mg.getDisplayName());
            row.put("blockDuration", mg.getBlockDuration().name());
            row.put("blockDurationSeconds", mg.getBlockDuration().getSeconds());
            row.put("binanceStream", mg.getBinanceStream());
            row.put("active", mg.isActive());
            row.put("running", activeGroups.containsKey(mg.getSlugPrefix()));
            result.add(row);
        }
        return result;
    }

    public MarketGroup add(String slugPrefix, String displayName, BlockDuration blockDuration, String binanceStream) {
        Optional<MarketGroup> existing = repository.findBySlugPrefix(slugPrefix);
        if (existing.isPresent()) return existing.get();

        MarketGroup mg = MarketGroup.builder()
                .slugPrefix(slugPrefix)
                .displayName(displayName)
                .blockDuration(blockDuration)
                .binanceStream(binanceStream)
                .active(false)
                .build();
        return repository.save(mg);
    }

    public void start(String id) {
        repository.findById(id).ifPresent(mg -> {
            startInternal(mg);
            mg.setActive(true);
            repository.save(mg);
            log.info("[MarketGroup] Started: {}", mg.getSlugPrefix());
        });
    }

    public void pause(String id) {
        repository.findById(id).ifPresent(mg -> {
            stopInternal(mg);
            mg.setActive(false);
            repository.save(mg);
            log.info("[MarketGroup] Paused: {}", mg.getSlugPrefix());
        });
    }

    public void remove(String id) {
        repository.findById(id).ifPresent(mg -> {
            stopInternal(mg);
            repository.delete(mg);
            log.info("[MarketGroup] Removed: {}", mg.getSlugPrefix());
        });
    }

    public Collection<MarketGroup> getActiveGroups() {
        return activeGroups.values();
    }

    public MarketGroup getActiveGroup(String slugPrefix) {
        return activeGroups.get(slugPrefix);
    }

    public BlockDuration getBlockDuration(String slugPrefix) {
        MarketGroup mg = activeGroups.get(slugPrefix);
        if (mg == null) {
            log.warn("[MarketGroup] Group '{}' not found in activeGroups, defaulting to FIVE_MIN", slugPrefix);
            return BlockDuration.FIVE_MIN;
        }
        return mg.getBlockDuration();
    }

    public String getDisplayName(String slugPrefix) {
        return displayNames.getOrDefault(slugPrefix, slugPrefix);
    }

    public Set<String> getGroupsForStream(String binanceStream) {
        return streamToGroups.getOrDefault(binanceStream, Collections.emptySet());
    }

    public ExecutorService getExecutor(String slugPrefix) {
        return groupExecutors.get(slugPrefix);
    }

    public List<BlockDuration> getAvailableDurations() {
        return List.of(BlockDuration.values());
    }

    private void startInternal(MarketGroup mg) {
        activeGroups.put(mg.getSlugPrefix(), mg);
        displayNames.put(mg.getSlugPrefix(), mg.getDisplayName());

        groupExecutors.computeIfAbsent(mg.getSlugPrefix(), prefix -> {
            String threadName = "mktgrp-" + prefix.replace("-", "").replace("_", "");
            log.info("[MarketGroup] Created executor: {}", threadName);
            return Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, threadName);
                t.setDaemon(true);
                return t;
            });
        });

        streamToGroups.computeIfAbsent(mg.getBinanceStream(), k -> ConcurrentHashMap.newKeySet())
                .add(mg.getSlugPrefix());
    }

    private void stopInternal(MarketGroup mg) {
        activeGroups.remove(mg.getSlugPrefix());
        displayNames.remove(mg.getSlugPrefix());

        ExecutorService exec = groupExecutors.remove(mg.getSlugPrefix());
        if (exec != null) exec.shutdown();

        Set<String> groups = streamToGroups.get(mg.getBinanceStream());
        if (groups != null) {
            groups.remove(mg.getSlugPrefix());
            if (groups.isEmpty()) streamToGroups.remove(mg.getBinanceStream());
        }
    }

    @PreDestroy
    public void shutdown() {
        groupExecutors.values().forEach(ExecutorService::shutdown);
        groupExecutors.clear();
    }
}
