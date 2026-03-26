package br.com.yacamin.rafael.application.service.cache.dto.trend;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.averages.SMAIndicator;

@Data
@Builder
public class SmaCacheDto implements CacheDto {
    private SMAIndicator indicator;
}