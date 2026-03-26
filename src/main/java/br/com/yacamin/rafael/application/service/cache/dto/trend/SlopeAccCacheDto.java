package br.com.yacamin.rafael.application.service.cache.dto.trend;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.DifferenceIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlopeAccCacheDto implements CacheDto {
    private DifferenceIndicator indicator;
}
