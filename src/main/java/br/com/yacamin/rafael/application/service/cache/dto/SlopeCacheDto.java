package br.com.yacamin.rafael.application.service.cache.dto;

import br.com.yacamin.rafael.application.service.indicator.extension.LinearRegressionSlopeIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlopeCacheDto implements CacheDto {
    private LinearRegressionSlopeIndicator indicator;
}
