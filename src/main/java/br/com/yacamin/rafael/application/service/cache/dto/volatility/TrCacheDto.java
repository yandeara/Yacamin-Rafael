package br.com.yacamin.rafael.application.service.cache.dto.volatility;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.TRIndicator;

@Data
@Builder
public class TrCacheDto implements CacheDto {
    private TRIndicator indicator;
}

