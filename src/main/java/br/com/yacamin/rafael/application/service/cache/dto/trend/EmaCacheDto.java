package br.com.yacamin.rafael.application.service.cache.dto.trend;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.averages.EMAIndicator;

@Data
@Builder
public class EmaCacheDto implements CacheDto {
    private EMAIndicator indicator;
}
