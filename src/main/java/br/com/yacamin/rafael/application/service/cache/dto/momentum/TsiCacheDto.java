package br.com.yacamin.rafael.application.service.cache.dto.momentum;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.TsiIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TsiCacheDto implements CacheDto {
    private TsiIndicator indicator;
}
