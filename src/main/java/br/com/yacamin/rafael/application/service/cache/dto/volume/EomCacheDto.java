package br.com.yacamin.rafael.application.service.cache.dto.volume;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import br.com.yacamin.rafael.application.service.indicator.volume.extension.EomSmoothedIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EomCacheDto implements CacheDto {
    private EomSmoothedIndicator indicator;
}
