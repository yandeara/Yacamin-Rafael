package br.com.yacamin.rafael.application.service.cache.dto;

import br.com.yacamin.rafael.application.service.indicator.extension.RollCovPctIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RollCovPctCacheDto implements CacheDto {
    private RollCovPctIndicator indicator;
}
