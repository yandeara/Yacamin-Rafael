package br.com.yacamin.rafael.application.service.cache.dto.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.ATRIndicator;

@Data
@Builder
public class AtrCacheDto implements CacheDto {
    private ATRIndicator indicator;
}

