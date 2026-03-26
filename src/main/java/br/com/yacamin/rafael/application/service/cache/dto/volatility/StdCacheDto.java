package br.com.yacamin.rafael.application.service.cache.dto.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

@Data
@Builder
public class StdCacheDto implements CacheDto {
    private StandardDeviationIndicator indicator;
}
