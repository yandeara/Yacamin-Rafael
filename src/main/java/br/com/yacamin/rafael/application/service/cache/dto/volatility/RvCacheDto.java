package br.com.yacamin.rafael.application.service.cache.dto.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import br.com.yacamin.rafael.application.service.indicator.extension.RealizedVolIndicator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RvCacheDto implements CacheDto {
    private RealizedVolIndicator indicator;
}
