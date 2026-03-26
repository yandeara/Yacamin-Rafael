package br.com.yacamin.rafael.application.service.cache.dto;

import br.com.yacamin.rafael.application.service.indicator.extension.VwapIndicator;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Data
@Builder
public class VwapCacheDto implements CacheDto {

    private VwapIndicator indicator;

}
