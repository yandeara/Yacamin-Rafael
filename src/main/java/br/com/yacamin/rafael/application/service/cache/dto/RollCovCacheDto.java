package br.com.yacamin.rafael.application.service.cache.dto;

import br.com.yacamin.rafael.application.service.indicator.extension.RollCovIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RollCovCacheDto implements CacheDto {
    private RollCovIndicator indicator;
}
