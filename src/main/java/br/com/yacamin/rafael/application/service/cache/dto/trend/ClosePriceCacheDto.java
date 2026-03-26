package br.com.yacamin.rafael.application.service.cache.dto.trend;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Data
@Builder
public class ClosePriceCacheDto implements CacheDto {

    private ClosePriceIndicator indicator;

}
