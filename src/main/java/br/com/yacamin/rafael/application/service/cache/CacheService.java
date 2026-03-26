package br.com.yacamin.rafael.application.service.cache;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import java.util.HashMap;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Getter
    private final HashMap<String, CacheDto> cache = new HashMap<>();

    public void put(String key, CacheDto indicator) {
        cache.put(key, indicator);
    }

    public CacheDto get(String key) {
        return cache.get(key);
    }


}
