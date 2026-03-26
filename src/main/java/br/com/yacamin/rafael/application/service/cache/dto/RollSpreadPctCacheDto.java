package br.com.yacamin.rafael.application.service.cache.dto;

import br.com.yacamin.rafael.application.service.indicator.extension.RollSpreadPctIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RollSpreadPctCacheDto implements CacheDto {
    private RollSpreadPctIndicator indicator;
}