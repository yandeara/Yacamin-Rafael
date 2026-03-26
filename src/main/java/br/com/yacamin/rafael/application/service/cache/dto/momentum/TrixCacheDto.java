package br.com.yacamin.rafael.application.service.cache.dto.momentum;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.TrixIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrixCacheDto implements CacheDto {
    private TrixIndicator indicator;
}