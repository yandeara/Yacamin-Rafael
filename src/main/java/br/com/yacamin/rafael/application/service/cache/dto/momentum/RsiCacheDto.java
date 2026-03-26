package br.com.yacamin.rafael.application.service.cache.dto.momentum;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.RSIIndicator;

@Data
@Builder
public class RsiCacheDto implements CacheDto {

    private RSIIndicator indicator;

}
