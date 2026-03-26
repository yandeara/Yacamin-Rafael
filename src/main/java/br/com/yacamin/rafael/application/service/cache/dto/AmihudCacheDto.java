package br.com.yacamin.rafael.application.service.cache.dto;

import br.com.yacamin.rafael.application.service.indicator.extension.AmihudIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmihudCacheDto implements CacheDto {
    private AmihudIndicator indicator;
}
