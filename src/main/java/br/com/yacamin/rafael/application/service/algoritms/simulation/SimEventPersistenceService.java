package br.com.yacamin.rafael.application.service.algoritms.simulation;

import br.com.yacamin.rafael.adapter.out.persistence.SimEventRepository;
import br.com.yacamin.rafael.domain.SimEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimEventPersistenceService {

    private final SimEventRepository repository;

    @Async("mongoWriteExecutor")
    public void record(String algo, String marketGroup, String slug, String type, Object payload) {
        try {
            SimEvent event = SimEvent.builder()
                    .slug(slug)
                    .marketGroup(marketGroup != null ? marketGroup : "unknown")
                    .timestamp(System.currentTimeMillis())
                    .type(type)
                    .algorithm(algo)
                    .payload(payload)
                    .build();
            repository.save(event);
            log.debug("[SimEvent] Persisted: algo={}, type={}, group={}, slug={}", algo, type, marketGroup, slug);
        } catch (Exception e) {
            log.warn("[SimEvent] FAILED to persist: algo={}, type={}, slug={}, error={}", algo, type, slug, e.getMessage());
        }
    }
}
