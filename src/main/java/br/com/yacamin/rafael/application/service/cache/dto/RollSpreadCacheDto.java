package br.com.yacamin.rafael.application.service.cache.dto;

import br.com.yacamin.rafael.application.service.indicator.extension.RollSpreadIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RollSpreadCacheDto implements CacheDto {
    private RollSpreadIndicator indicator;
}