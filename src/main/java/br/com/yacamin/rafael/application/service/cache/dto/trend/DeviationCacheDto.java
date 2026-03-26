package br.com.yacamin.rafael.application.service.cache.dto.trend;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.DeviationIndicator;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DeviationCacheDto implements CacheDto {
    private final DeviationIndicator indicator;
}
