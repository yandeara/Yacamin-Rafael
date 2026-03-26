package br.com.yacamin.rafael.application.service.cache.dto.trend;

import br.com.yacamin.rafael.application.service.cache.dto.CacheDto;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;

@Data
@Builder
public class AdxCacheDto implements CacheDto {
    private ADXIndicator adx;
    private PlusDIIndicator pdi;
    private MinusDIIndicator mdi;
}
