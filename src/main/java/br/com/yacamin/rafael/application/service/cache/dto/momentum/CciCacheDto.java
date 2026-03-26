package br.com.yacamin.rafael.application.service.cache.dto.momentum;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.CCIIndicator;

@Data
@Builder
public class CciCacheDto implements CacheDto {

    private CCIIndicator indicator;

}
