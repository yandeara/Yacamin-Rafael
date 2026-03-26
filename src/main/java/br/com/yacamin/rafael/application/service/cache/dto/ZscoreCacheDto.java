package br.com.yacamin.rafael.application.service.cache.dto;

import br.com.yacamin.rafael.application.service.indicator.extension.ZscoreIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZscoreCacheDto implements CacheDto {
    private ZscoreIndicator indicator;
}
