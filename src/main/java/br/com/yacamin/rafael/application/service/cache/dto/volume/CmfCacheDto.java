package br.com.yacamin.rafael.application.service.cache.dto.volume;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;

@Data
@Builder
public class CmfCacheDto implements CacheDto {
    private ChaikinMoneyFlowIndicator indicator;
}
